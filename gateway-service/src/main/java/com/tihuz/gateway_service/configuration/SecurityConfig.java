package com.tihuz.gateway_service.configuration;

import com.tihuz.common.redis.BlacklistTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpMethod;
@Configuration
@EnableWebFluxSecurity // Enable Spring Security for WebFlux (Reactive)
public class SecurityConfig {

    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    String [] GET_ENDPOINTS ={ "/api/jobs/**","/api/companies/**" };
    String [] PUBLIC_ENDPOINTS ={"/api/auth/**","/fallback/**" };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http)
    {
        return http

                .cors(Customizer.withDefaults())
                // Disable CSRF protection because this is a stateless API (no session, no form login)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // check request
                .authorizeExchange(auth -> auth

                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(PUBLIC_ENDPOINTS ).permitAll()

                        .pathMatchers(HttpMethod.GET,GET_ENDPOINTS ).permitAll()
                        .pathMatchers(HttpMethod.POST,"/api/companies" ).permitAll()




                        .anyExchange().authenticated())


               // Configure the Gateway as a Resource Server
                .oauth2ResourceServer(oauth2->oauth2
                        .jwt(Customizer.withDefaults())

                        //Throw an unauthorized (401) exception for global handling (ErrorWebExceptionHandler)
                        .authenticationEntryPoint((exchange, ex) -> Mono.error(ex))

                )


//                // Custom security exception handled
//                .exceptionHandling(ex -> ex
//
//                        //Throw an unauthorized (401) exception for global handling
//                        .authenticationEntryPoint((swe, e) -> Mono.error(e))
//
//                        // Handle access denied (403), then rethrow
//                        .accessDeniedHandler((swe, e) -> Mono.error(e)))

                .build();
    }


    @Bean
    public ReactiveJwtDecoder jwtDecoder(BlacklistTokenService blacklistTokenService)
    {
        SecretKeySpec spec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(spec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        decoder.setJwtValidator(jwt ->
        {

            if (blacklistTokenService.exists(jwt.getTokenValue()))
            {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token")
                );
            }

            String type= jwt.getClaimAsString("type");
            if("refresh".equals(type))
            {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token"));

            }

            return OAuth2TokenValidatorResult.success();
        });

        return decoder;

    }
}