package com.tihuz.notification_service.consumer;

import com.tihuz.common.event.NewApplicationEvent;
import com.tihuz.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewApplicationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = NewApplicationEvent.TOPIC, groupId = "notification-service-group")
    public void consumeNewApplicationEvent(NewApplicationEvent event) {
        log.info("📥 Received NewApplicationEvent: applicationId={}, jobTitle={}, companyEmail={}, companyUserName={}",
                event.getApplicationId(), event.getJobTitle(), event.getCompanyEmail(), event.getCompanyUserName());

        if (event.getCompanyEmail() == null || event.getCompanyEmail().isBlank()) {
            log.warn("❌ Company email is missing, cannot send notification for application {}", event.getApplicationId());
            return;
        }

        // Gửi email cho chủ job (công ty)
        emailService.sendNewApplicationEmail(
                event.getCompanyEmail(),
                event.getCompanyUserName(),
                event.getCompanyName(),
                event.getJobTitle(),
                event.getApplicantName(),
                event.getApplicantEmail(),
                event.getCvUrl(),
                event.getCoverLetter()
        );
        log.info("✅ New application email sent to {}", event.getCompanyEmail());
    }
}