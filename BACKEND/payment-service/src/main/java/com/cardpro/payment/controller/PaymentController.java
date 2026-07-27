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

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(userId, request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(userId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<?>> getHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getTransactionHistory(userId, page, size));
    }
}
