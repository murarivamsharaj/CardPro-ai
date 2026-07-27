package com.cardpro.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CreateOrderResponse {
    private String orderId;
    private String razorpayKeyId;
    private int amount;
    private String currency;
    private String status;
}
