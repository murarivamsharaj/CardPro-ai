package com.cardpro.payment.controller;

import com.cardpro.payment.dto.request.CreateOrderRequest;
import com.cardpro.payment.dto.request.VerifyPaymentRequest;
import com.cardpro.payment.dto.response.CreateOrderResponse;
import com.cardpro.payment.dto.response.VerifyPaymentResponse;
import com.cardpro.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
// Accept requests from both the standard v1 path and the user-service gateway proxy path
@RequestMapping({"/api/v1/payments", "/api/users/payments"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            Principal principal,
            @RequestBody(required = false) CreateOrderRequest request) {

        String userId = resolveUserId(headerUserId, principal);

        // Handle empty bodies gracefully (e.g., when frontend triggers a default PRO upgrade)
        if (request == null) {
            request = new CreateOrderRequest();
            request.setAmount(999); // Fallback standard amount
        }

        return ResponseEntity.ok(paymentService.createOrder(userId, request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            Principal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(paymentService.verifyPayment(resolveUserId(headerUserId, principal), request));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<?>> getHistory(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(paymentService.getTransactionHistory(resolveUserId(headerUserId, principal), page, size));
    }

    private String resolveUserId(String headerUserId, Principal principal) {
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        if (principal != null && principal.getName() != null) return principal.getName();

        // 🚨 EMERGENCY DEMO BYPASS: Return a dummy UUID so Razorpay still generates the order 🚨
        return "123e4567-e89b-12d3-a456-426614174000";
    }
}