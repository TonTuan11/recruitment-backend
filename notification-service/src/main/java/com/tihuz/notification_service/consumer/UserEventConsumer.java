package com.tihuz.notification_service.consumer;

import com.tihuz.common.event.UserEvent;
import com.tihuz.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer
{

    private final EmailService emailService;

    @KafkaListener(topics = UserEvent.TOPIC, groupId = "notification-service-group")
    public void consumeUserEvent(UserEvent event)
    {
        log.info("Received UserEvent: userId={}, action={}, email={}", event.getUserId(), event.getAction(), event.getEmail());

        if ("CREATED".equals(event.getAction()))
        {
            // Send welcome email to newly registered user
            emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
            log.info("Welcome email sent to {}", event.getEmail());
        }
        else
        {
            log.debug("Skipping UserEvent action: {}", event.getAction());
        }
    }
}