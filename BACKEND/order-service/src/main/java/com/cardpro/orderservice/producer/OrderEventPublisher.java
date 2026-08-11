package com.cardpro.orderservice.producer;

import com.cardpro.orderservice.config.RabbitMQConfig;
import com.cardpro.orderservice.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order domain events to RabbitMQ for downstream services
 * (e.g. payment-service) to consume asynchronously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes an {@code OrderCreated} event to the {@code order.exchange}.
     * Failures are logged and swallowed so order creation never breaks
     * because the broker is temporarily unreachable.
     *
     * @param event the event to publish
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event);
            log.info("Published OrderCreated event for order id: {}", event.getOrderId());
        } catch (AmqpException ex) {
            log.error("Failed to publish OrderCreated event for order id: {}",
                    event.getOrderId(), ex);
        }
    }
}
