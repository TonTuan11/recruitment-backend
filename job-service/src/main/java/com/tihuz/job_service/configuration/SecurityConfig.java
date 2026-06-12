package com.tihuz.job_service.configuration;

import com.tihuz.common.configuration.JwtAuthenticationEntryPoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/jobs/**").permitAll()
                        .requestMatchers("/jobs/check-apply").authenticated()

                        .anyRequest().authenticated())


                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt
                                        (jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)

                                         )
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );


        return http.build();
    }


}