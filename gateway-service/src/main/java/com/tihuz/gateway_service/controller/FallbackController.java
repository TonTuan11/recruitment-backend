package com.tihuz.gateway_service.controller;


import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // Trả về Mono vì Gateway chạy WebFlux → xử lý bất đồng bộ, không block thread
    @RequestMapping("/user-service-fallback")
    public Mono<ApiResponse<String>> userServiceFallback()
    {
        log.error("FALLBACK USER SERVICE GATEWAY");
        ErrorCode errorCode=ErrorCode.FALLBACK_TRIGGERED;
        return Mono.just(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                .build());
    }

    @RequestMapping("/job-service-fallback")
    public Mono<ApiResponse<String>> postServiceFallback()
    {
        log.error("FALLBACK JOB SERVICE GATEWAY");
        ErrorCode errorCode=ErrorCode.FALLBACK_TRIGGERED;
        return Mono.just(ApiResponse.<String>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                .build());
    }

    @RequestMapping("/application-service-fallback")
    public Mono<ApiResponse<String>> applicationServiceFallback()
    {
        log.error("FALLBACK APPLICATION SERVICE GATEWAY");
        ErrorCode errorCode=ErrorCode.FALLBACK_TRIGGERED;
        return Mono.just(ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }



    @RequestMapping("/company-service-fallback")
    public Mono<ApiResponse<String>> companyServiceFallback()
    {
        log.error("FALLBACK COMPANY SERVICE GATEWAY");
        ErrorCode errorCode=ErrorCode.FALLBACK_TRIGGERED;
        return Mono.just(ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }


    @RequestMapping("/media-service-fallback")
    public Mono<ApiResponse<String>> mediaServiceFallback()
    {
        log.error("FALLBACK MEDIA SERVICE GATEWAY");
        ErrorCode errorCode=ErrorCode.FALLBACK_TRIGGERED;
        return Mono.just(ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }
}
