package com.cardpro.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Razorpay Checkout callback payload — the same fields Razorpay passes to the
 * browser {@code handler} after a successful payment.
 */
@Data
public class VerifyPaymentRequest {

    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;

    @NotBlank(message = "signature is required")
    private String signature;
}
