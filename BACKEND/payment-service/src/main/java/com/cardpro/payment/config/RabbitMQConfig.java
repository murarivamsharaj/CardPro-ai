package com.cardpro.payment.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
 *
 * <p>The {@code cardpro.events.exchange} topic exchange is where this service
 * publishes {@code payment.completed} events after a payment is verified, so
 * other services can unlock the purchased entitlement.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String CARD_PRO_EVENTS_EXCHANGE = "cardpro.events.exchange";

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public TopicExchange cardProEventsExchange() {
        return new TopicExchange(CARD_PRO_EVENTS_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Explicit RabbitTemplate wired with the JSON converter so outbound events
     * are published as JSON rather than serialized Java objects.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
