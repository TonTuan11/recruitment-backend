package com.tihuz.notification_service.consumer;

import com.tihuz.common.event.ApplicationStatusEvent;

import com.tihuz.notification_service.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ApplicationStatusEventConsumer
{

      EmailService emailService;

    @KafkaListener(topics = ApplicationStatusEvent.TOPIC, groupId = "notification-service-group")
    public void consumeApplicationStatusEvent(ApplicationStatusEvent event) {
        log.info("Received ApplicationStatusEvent: applicationId={}, userId={}, status={}",
                event.getApplicationId(), event.getUserId(), event.getNewStatus());

        // Kiểm tra có email không
        if (event.getUserEmail() == null || event.getUserEmail().isBlank()) {
            log.warn("Cannot send email: user email is missing for userId={}", event.getUserId());
            return;
        }

        // Map status -> tiếng Việt
        String statusLabel = switch (event.getNewStatus())
        {
            case "ACCEPTED" -> "đã duyệt";
            case "REJECTED" -> "đã từ chối";
            default -> event.getNewStatus();
        };

        emailService.sendStatusChangeEmail(
                event.getUserEmail(),
                event.getUserUsername(),
                event.getJobTitle(),
                statusLabel
        );
        log.info("Status change email sent to {}", event.getUserEmail());
    }
}