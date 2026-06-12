package com.tihuz.gateway_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration

// The Gateway has multiple exception handlers
// Lower values run earlier ( @Order (values))
// -2 is higher than Spring’s default handler
@Order(-2)
@RequiredArgsConstructor

//Class this catch all exceptions at the Gateway (before requests services like: user-service, post-service).
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) // exchange: request/response,  ex: exception is thrown.
    {

        log.error("===> MESSAGE ERROR: {}", ex.getMessage());
        log.error("===> CLASS: {}", ex.getClass().getName());


        // Convert exception -> ErrorCode
         ErrorCode errorCode = determineErrorCode(ex);

         // Set response
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(errorCode.getStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Return json to client
        return response.writeWith
                (Mono.fromCallable
                    (
                     () ->
                        {
                            ApiResponse<?> apiResponse = ApiResponse.builder()
                                    .code(errorCode.getCode())
                                    .message(errorCode.getMessage())
                                    .build();

                            return response.bufferFactory().wrap(objectMapper.writeValueAsBytes(apiResponse));
                        }
                    )
                );
    }

    // Mapping exception → ErrorCode
    private ErrorCode determineErrorCode(Throwable ex)
    {

        if( ex instanceof AuthenticationCredentialsNotFoundException )
        {
            return ErrorCode.UNAUTHORIZED_EXCEPTION;
        }

        if (ex instanceof InvalidBearerTokenException || ex instanceof JwtException || ex instanceof OAuth2AuthenticationException)
        {
            String msg = (ex.getMessage() != null) ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("expired"))
            {
                return ErrorCode.TOKEN_EXPIRED;
            }
            return ErrorCode.TOKEN_INVALID; // Other JWT errors is invalid
        }

        // If it is a ResponseStatusException (thrown by the Gateway)
        // ex: throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (ex instanceof ResponseStatusException rs)
        {
            if (rs.getStatusCode() == HttpStatus.NOT_FOUND)
            {
                return ErrorCode.API_NOT_FOUND;
            }
            if (rs.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
            {
                return ErrorCode.SERVICE_UNAVAILABLE;
            }
        }

        if (ex instanceof WebClientRequestException ||
                ex instanceof WebClientResponseException) {
            return ErrorCode.DEPENDENCY_FAILED; // 503
        }

        // Check Errors from services / Eureka / network.
        String message = (ex.getMessage() != null) ? ex.getMessage() : "";

        if(message.contains("Connection refused"))
        {
            return ErrorCode.CONNECTION_REFUSED;
        }

        if (message.contains("Unable to find instance") ||   // Eureka not found service
                message.contains("503 SERVICE_UNAVAILABLE")  // Service down
        )

        {
            return ErrorCode.SERVICE_UNAVAILABLE;
        }

        return ErrorCode.UNCATEGORIZED_EXCEPTION;
    }
}