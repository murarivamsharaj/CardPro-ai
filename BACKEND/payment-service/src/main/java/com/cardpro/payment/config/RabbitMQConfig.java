package com.cardpro.payment.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the payment-service.
 *
 * <p>The {@code order.created.queue} is declared here as well (idempotently)
 * so it exists even if payment-service boots before order-service. The
 * exchange and binding live in order-service, which owns the topology.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
