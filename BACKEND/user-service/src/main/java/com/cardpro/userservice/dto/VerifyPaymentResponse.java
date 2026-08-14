package com.cardpro.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Outcome of POST /payments/verify. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentResponse {

    private boolean success;

    private String message;

    /** The user's Pro status after verification. */
    private boolean pro;
}
