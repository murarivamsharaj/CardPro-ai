package com.cardpro.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private String orderId;

    @JsonProperty("razorpayKeyId")
    private String razorpayKeyId;

    @JsonProperty("key")
    private String key;

    private int amount;
    private String currency;
    private String status;
}