package com.cardpro.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@Slf4j
public class WebhookVerificationService {

    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    public boolean verifyWebhook(String payload, String signature) {
        try {
            // 1. Initialize the HMAC SHA256 Mac instance
            Mac mac = Mac.getInstance("HmacSHA256");

            // 2. Create the secret key using your webhook secret
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            // 3. Hash the incoming payload
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // 4. Convert the byte array to a Hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String generatedSignature = hexString.toString();

            // 5. Compare signatures safely (MessageDigest.isEqual prevents timing attacks)
            return MessageDigest.isEqual(
                    generatedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );

            // 6. Properly close the try block with a catch!
        } catch (Exception e) {
            log.error("Webhook signature verification failed due to an exception", e);
            return false;
        }
    }
}