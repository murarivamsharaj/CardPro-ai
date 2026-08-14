package com.cardpro.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload returned by POST /payments/create-order. The browser needs the
 * public Razorpay Key ID (safe to expose) to open the Checkout modal with the
 * pre-created order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private String orderId;

    /** Public Razorpay Key ID used to initialize the Checkout modal. */
    private String keyId;

    /** Order amount in paise (₹999 → 99900). */
    private int amount;

    private String currency;

    private String status;
}
