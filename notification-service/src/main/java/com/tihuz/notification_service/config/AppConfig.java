package com.tihuz.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AppConfig
{
    // RestTemplate for calling external APIs (feign preferred)
//    @Bean
//    public RestTemplate restTemplate()
//    {
//        return new RestTemplate();
//    }

    // Async executor for sending emails (non-blocking)
    // Spring creates an Executor for methods annotated with @Async("emailExecutor")
    @Bean(name = "emailExecutor")  //Thread Pool
    public Executor emailExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);  //Always keeps 2 threads active
        executor.setMaxPoolSize(5);   // max threads
        executor.setQueueCapacity(100); //Maximum number of queued tasks.
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }
}