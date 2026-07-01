//package com.tihuz.notification_service.config;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import java.util.Properties;
//
//
//// This class creates a `JavaMailSender`
//@Configuration
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class MailConfig
//{
//
//    @Value("${spring.mail.host}")
//      String host;
//
//    @Value("${spring.mail.port}")
//     int port;
//
//    @Value("${spring.mail.username}")
//     String username;
//
//    @Value("${spring.mail.password}")
//     String password;
//
//    @Bean
//    public JavaMailSender javaMailSender()
//    {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(host);
//        mailSender.setPort(port);
//        mailSender.setUsername(username);
//        mailSender.setPassword(password);
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.starttls.required", "true");
//        props.put("mail.debug", "false");
//
//        return mailSender;
//    }
//}