package com.cardpro.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VerifyPaymentResponse {
    private boolean success;
    private String message;
}
