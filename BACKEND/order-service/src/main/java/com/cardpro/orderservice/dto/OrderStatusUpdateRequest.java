package com.cardpro.orderservice.dto;

import com.cardpro.orderservice.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload for updating an order's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
