package com.cardpro.payment.consumer;

import com.cardpro.payment.config.RabbitMQConfig;
import com.cardpro.payment.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order-created events published by the order-service.
 */
@Slf4j
@Component
public class OrderCreatedConsumer {

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event: {}", event);
    }
}
