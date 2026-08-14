package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.CreateOrderResponse;
import com.cardpro.userservice.dto.VerifyPaymentRequest;
import com.cardpro.userservice.dto.VerifyPaymentResponse;
import com.cardpro.userservice.service.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Razorpay-backed CardPro Pro upgrades.
 *
 * <p>The caller is the authenticated user identified by the JWT email claim
 * ({@code principal.getName()}) — same convention as {@link ProfileController}.
 *
 * <p>Mapped at BOTH {@code /api/users/payments} (user-service's native path,
 * which the frontend already uses to call :8083 directly) and
 * {@code /api/v1/users/payments} (the versioned convention), so either works.
 * user-service is not routed through the gateway, so neither path touches
 * gateway-service.
 */
@RestController
@RequestMapping({"/api/users/payments", "/api/v1/users/payments"})
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay Pro subscription endpoints (authenticated)")
public class PaymentController {

    private final RazorpayService razorpayService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a Razorpay order for the ₹999 Pro upgrade")
    public ResponseEntity<CreateOrderResponse> createOrder(Principal principal) {
        return ResponseEntity.ok(razorpayService.createProOrder(principal.getName()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify the Razorpay signature and activate Pro")
    public ResponseEntity<VerifyPaymentResponse> verify(
            Principal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(razorpayService.verifyProPayment(principal.getName(), request));
    }
}