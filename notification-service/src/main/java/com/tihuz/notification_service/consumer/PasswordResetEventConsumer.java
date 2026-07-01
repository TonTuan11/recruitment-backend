package com.tihuz.notification_service.consumer;

import com.tihuz.common.event.PasswordResetEvent;
import com.tihuz.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = PasswordResetEvent.TOPIC, groupId = "notification-service-group")
    public void consumePasswordResetEvent(PasswordResetEvent event)
    {
        log.info("Received PasswordResetEvent: userId={}, email={}", event.getUserId(), event.getEmail());

        emailService.sendResetPasswordEmail(event.getEmail(), event.getUsername(), event.getOtp());

        log.info("Password reset email sent to {}", event.getEmail());
    }
}