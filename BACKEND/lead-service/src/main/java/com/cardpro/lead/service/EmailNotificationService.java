package com.cardpro.lead.service;

import com.cardpro.lead.client.CardProfileResponse;
import com.cardpro.lead.client.CardServiceClient;
import com.cardpro.lead.entity.Lead;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Sends email notifications about newly captured leads to the owning card owner.
 * <p>
 * The send happens asynchronously so a slow or failing SMTP call never blocks
 * the HTTP thread that handled the lead submission.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final CardServiceClient cardServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    @Value("${spring.mail.username}")
    private String mailUsername;

    /**
     * Notifies the owner of the card the lead was submitted against.
     * Failures are logged and swallowed so they cannot affect the lead flow.
     */
    @Async
    public void sendNewLeadEmail(Lead lead) {
        try {
            CardProfileResponse cardProfile = cardServiceClient.getProfileById(lead.getProfileId(), internalApiKey);
            String ownerEmail = extractOwnerEmail(cardProfile);
            if (ownerEmail == null || ownerEmail.isBlank()) {
                log.warn("No owner email on profile {}; skipping new-lead notification for lead {}",
                    lead.getProfileId(), lead.getId());
                return;
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailUsername);
            helper.setTo(ownerEmail);
            helper.setSubject("You have a new lead from " + lead.getVisitorName() + "!");
            helper.setText(buildHtmlBody(lead), true);
            mailSender.send(mimeMessage);

            log.info("New-lead notification email sent to {} for lead {}", ownerEmail, lead.getId());
        } catch (Exception e) {
            log.error("Failed to send new-lead notification email for lead {}", lead.getId(), e);
        }
    }

    /**
     * The card owner's email lives inside the card's {@code profileData} JSON
     * (see the CardPro create-card form, which stores {@code email} there).
     */
    private String extractOwnerEmail(CardProfileResponse cardProfile) {
        if (cardProfile == null || cardProfile.profileData() == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(cardProfile.profileData());
            return node.path("email").asText(null);
        } catch (Exception e) {
            log.warn("Could not parse profileData for card {}: {}", cardProfile.id(), e.getMessage());
            return null;
        }
    }

    private String buildHtmlBody(Lead lead) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, Helvetica, sans-serif; color: #1f2937; background-color: #f9fafb; margin: 0; padding: 0;">
              <div style="max-width: 600px; margin: 0 auto; padding: 24px;">
                <h2 style="margin: 0 0 8px; color: #111827;">New lead captured!</h2>
                <p style="margin: 0 0 24px; color: #6b7280;">
                  Someone just submitted your card. Here are the details:
                </p>
                <table style="width: 100%%; border-collapse: collapse; background-color: #ffffff; border: 1px solid #e5e7eb; border-radius: 8px;">
                  <tr>
                    <td style="padding: 12px 16px; font-weight: bold; width: 140px; border-bottom: 1px solid #f3f4f6;">Name</td>
                    <td style="padding: 12px 16px; border-bottom: 1px solid #f3f4f6;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 12px 16px; font-weight: bold; width: 140px; border-bottom: 1px solid #f3f4f6;">Email</td>
                    <td style="padding: 12px 16px; border-bottom: 1px solid #f3f4f6;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 12px 16px; font-weight: bold; width: 140px; border-bottom: 1px solid #f3f4f6;">Phone</td>
                    <td style="padding: 12px 16px; border-bottom: 1px solid #f3f4f6;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 12px 16px; font-weight: bold; width: 140px;">Message</td>
                    <td style="padding: 12px 16px;">%s</td>
                  </tr>
                </table>
                <p style="margin-top: 24px; color: #9ca3af; font-size: 12px;">
                  Lead ID: %s &middot; Captured at: %s
                </p>
              </div>
            </body>
            </html>
            """.formatted(
            htmlEscape(lead.getVisitorName()),
            htmlEscape(lead.getVisitorEmail()),
            htmlEscape(lead.getVisitorPhone()),
            htmlEscape(lead.getMessage()),
            lead.getId(),
            lead.getCapturedAt()
        );
    }

    private String htmlEscape(String value) {
        if (value == null) {
            return "&mdash;";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
