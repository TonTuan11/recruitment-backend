package com.tihuz.job_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.tihuz.job_service", "com.tihuz.common"})
//@EnableJpaAuditing
@EnableFeignClients
public class JobServiceApplication {

	public static void main(String[] args)
    {
		SpringApplication.run(JobServiceApplication.class, args);
	}

}
