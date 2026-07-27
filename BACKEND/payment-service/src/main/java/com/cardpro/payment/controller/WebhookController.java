package com.cardpro.payment.controller;

import com.cardpro.payment.service.WebhookVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookVerificationService webhookVerificationService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        boolean isValid = webhookVerificationService.verifyWebhook(payload, signature);
        if (isValid) {
            return ResponseEntity.ok("Webhook received");
        }
        return ResponseEntity.status(400).body("Invalid signature");
    }
}
