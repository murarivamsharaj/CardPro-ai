package com.cardpro.payment.dto.request;

import com.cardpro.payment.enums.ItemType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Item type is required")
    private ItemType itemType;
}
