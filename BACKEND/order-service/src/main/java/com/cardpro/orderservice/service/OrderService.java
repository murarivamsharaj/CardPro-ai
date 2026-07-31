package com.cardpro.orderservice.service;

import com.cardpro.orderservice.dto.OrderRequest;
import com.cardpro.orderservice.dto.OrderResponse;
import com.cardpro.orderservice.dto.OrderStatusUpdateRequest;
import com.cardpro.orderservice.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrders(Long userId);

    OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);

    void cancelOrder(Long id);
}
