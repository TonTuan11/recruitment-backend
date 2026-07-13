//package com.tihuz.application_service.configuration;
//
//import feign.Capability;
//import feign.micrometer.MicrometerObservationCapability;
//import io.micrometer.observation.ObservationRegistry;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FeignConfig
//{
//
//    @Bean
//    public Capability capability(ObservationRegistry registry)
//    {
//        // Class này giúp Feign tự động đính kèm Trace ID vào Header khi gọi sang Service khác
//        return new MicrometerObservationCapability(registry);
//    }
//}