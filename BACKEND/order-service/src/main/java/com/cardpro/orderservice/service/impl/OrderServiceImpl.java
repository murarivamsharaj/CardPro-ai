package com.cardpro.orderservice.service.impl;

import com.cardpro.orderservice.client.ProductServiceClient;
import com.cardpro.orderservice.dto.OrderRequest;
import com.cardpro.orderservice.dto.OrderResponse;
import com.cardpro.orderservice.dto.OrderStatusUpdateRequest;
import com.cardpro.orderservice.dto.ProductResponse;
import com.cardpro.orderservice.entity.Order;
import com.cardpro.orderservice.enums.OrderStatus;
import com.cardpro.orderservice.exception.OrderNotFoundException;
import com.cardpro.orderservice.exception.ProductUnavailableException;
import com.cardpro.orderservice.repository.OrderRepository;
import com.cardpro.orderservice.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the OrderService interface.
 * Handles the business logic for order CRUD operations,
 * including product validation via the product-service Feign client.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.debug("Creating order for user id: {}, product id: {}", request.getUserId(), request.getProductId());

        // Validate the product exists and fetch its price via inter-service call
        ProductResponse product = validateProductAvailability(request.getProductId());

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);
        log.info("Order created successfully with id: {}", order.getId());

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long userId) {
        log.debug("Fetching orders for user id: {}", userId);

        List<Order> orders = (userId != null)
                ? orderRepository.findByUserId(userId)
                : orderRepository.findAll();

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        log.debug("Updating status of order with id: {} to {}", id, request.getStatus());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(request.getStatus());
        order = orderRepository.save(order);
        log.info("Order status updated successfully for order id: {}", order.getId());

        return mapToResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.debug("Cancelling order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled successfully with id: {}", id);
    }

    /**
     * Validates that the referenced product exists by calling the product-service.
     *
     * @param productId the product to validate
     * @return the product response fetched from product-service
     * @throws ProductUnavailableException if the call fails or the product is null
     */
    private ProductResponse validateProductAvailability(Long productId) {
        try {
            ProductResponse product = productServiceClient.getProductById(productId);
            if (product == null) {
                throw new ProductUnavailableException(productId);
            }
            return product;
        } catch (FeignException ex) {
            // A 404 means the product does not exist — report it as unavailable,
            // reserving the service-down message for other failures.
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ProductUnavailableException(productId);
            }
            log.error("Product-service call failed for product id: {}", productId, ex);
            throw new ProductUnavailableException(productId,
                    "Product service is unavailable. Please try again later.");
        }
    }

    /**
     * Maps Order entity to OrderResponse DTO.
     *
     * @param order the entity to map
     * @return the response DTO
     */
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
