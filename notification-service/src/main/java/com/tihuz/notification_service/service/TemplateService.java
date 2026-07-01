package com.tihuz.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class TemplateService
{

    private final SpringTemplateEngine templateEngine;

    // Render welcome email template
    public String renderWelcome(String username)
    {
        Context context = new Context();
        context.setVariable("username", username);
        return templateEngine.process("welcome", context);
    }

    // Render status change email template
    public String renderStatusChange(String username, String jobTitle, String statusLabel)
    {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("status", statusLabel);
        return templateEngine.process("status-change", context);
    }

    // Render reset password email template
    public String renderResetPassword(String username, String otp)
    {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otp", otp);
        return templateEngine.process("reset-password", context);
    }
}