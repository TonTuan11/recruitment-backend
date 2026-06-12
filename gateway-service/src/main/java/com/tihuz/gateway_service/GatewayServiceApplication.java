package com.tihuz.gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.tihuz.gateway_service",
        "com.tihuz.common.dto",
        "com.tihuz.common.exception"
})

public class GatewayServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
