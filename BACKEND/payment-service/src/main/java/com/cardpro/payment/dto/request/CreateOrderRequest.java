package com.cardpro.payment.dto.request;

import com.cardpro.payment.enums.ItemType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    /** Amount in rupees. Optional — when omitted the catalogue price for the item type is used. */
    private Integer amount;

    /** Client-supplied receipt reference. Optional — a receipt is generated when omitted. */
    private String receiptId;
}
