package com.tihuz.notification_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class EmailService
{

      JavaMailSender mailSender;
      TemplateService templateService;

    // Send email with HTML content
    private void sendMail(String to, String subject, String body)
    {
        try
        {
            MimeMessage message = mailSender.createMimeMessage();  //create mail
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);  // Recipient email
            helper.setSubject(subject);  // Title
            helper.setText(body, true);  // true -> html
            mailSender.send(message);  // send
            log.info("Email sent to: {}", to);
        }
        catch (MessagingException e)
        {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // Welcome email for new user registration
    @Async("emailExecutor")
    public void sendWelcomeEmail(String to, String username)
    {
        String body = templateService.renderWelcome(username);
        sendMail(to, "Chào mừng bạn đến với TizJob!", body);
    }

    // Email when application status changes (ACCEPTED / REJECTED)
    @Async("emailExecutor")
    public void sendStatusChangeEmail(String to, String username, String jobTitle, String statusLabel) {
        String body = templateService.renderStatusChange(username, jobTitle, statusLabel);
        sendMail(to, "Cập nhật trạng thái đơn ứng tuyển", body);
    }

    // Email with reset password link
    @Async("emailExecutor")
    public void sendResetPasswordEmail(String to, String username, String otp)
    {
        String body = templateService.renderResetPassword(username, otp);
        sendMail(to, "Đặt lại mật khẩu TizJob", body);
    }
}