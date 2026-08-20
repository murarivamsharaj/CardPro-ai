package com.cardpro.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    /**
     * Sends an async security alert email when a user logs in.
     * Fails silently so it never blocks the authentication flow.
     */
    @Async
    public void sendLoginAlertEmail(String userEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(mailUsername);
            helper.setTo(userEmail);
            helper.setSubject("Security Alert: New Login to CardPro AI");

            String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px;">
                    <h2 style="color: #111827;">New Login Detected</h2>
                    <p style="color: #4b5563;">We noticed a new login to your CardPro AI account just now.</p>
                    <p style="color: #4b5563;">If this was you, you can safely ignore this email. If you did not log in, please reset your password immediately to secure your account.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;" />
                    <p style="color: #9ca3af; font-size: 12px;">This is an automated security message from CardPro AI.</p>
                </div>
                """;

            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);

            log.info("Security login email successfully sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send security login email to {}", userEmail, e);
        }
    }
}
