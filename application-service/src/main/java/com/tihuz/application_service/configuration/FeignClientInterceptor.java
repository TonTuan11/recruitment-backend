package com.tihuz.application_service.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            log.warn("NO REQUEST CONTEXT");
            return;
        }

       // SecurityContextHolder gọi xuống service khác / nhảy thread → mất header

        //Gắn với HTTP request mà service hiện tại nhận được → vẫn lấy được header để dùng tiếp
        HttpServletRequest request = attrs.getRequest();

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            requestTemplate.header("Authorization", authHeader);
            log.info("TOKEN FORWARDED");
        } else
        {
            log.warn("NO AUTH HEADER");
        }
    }
}