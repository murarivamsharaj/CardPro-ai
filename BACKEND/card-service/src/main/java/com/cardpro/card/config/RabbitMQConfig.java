package com.cardpro.card.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for card-service.
 *
 * <p>Declares the {@code cardpro.events.exchange} topic exchange and binds a
 * durable queue to it with the {@code payment.completed.#} pattern, so
 * {@link com.cardpro.card.dto.event.PaymentCompletedEvent} messages published
 * by payment-service are delivered here. The {@link Jackson2JsonMessageConverter}
 * ensures the incoming payload is deserialized as JSON into the event record.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CARD_PRO_EVENTS_EXCHANGE = "cardpro.events.exchange";
    public static final String PAYMENT_EVENTS_QUEUE = "card-service.payment.events.queue";

    @Bean
    public TopicExchange cardProEventsExchange() {
        return new TopicExchange(CARD_PRO_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue paymentEventsQueue() {
        return new Queue(PAYMENT_EVENTS_QUEUE, true); // durable
    }

    @Bean
    public Binding paymentEventsBinding(Queue paymentEventsQueue, TopicExchange cardProEventsExchange) {
        return BindingBuilder.bind(paymentEventsQueue).to(cardProEventsExchange).with("payment.completed.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
