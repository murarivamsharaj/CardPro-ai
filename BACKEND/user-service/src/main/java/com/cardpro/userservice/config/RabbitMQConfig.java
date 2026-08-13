package com.cardpro.userservice.config;

import com.cardpro.userservice.service.NotificationEventPublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for user notification-preference events.
 *
 * <p>The exchange is shared across CardPro services; this queue carries
 * {@code email.notifications.updated} events so the lead-notification pipeline
 * can honor per-user preferences. Declarations are lazy — if the broker is
 * down at startup the app still boots and the toggle still persists locally.
 */
@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_PREFERENCES_QUEUE = "user.notification.preferences";

    @Bean
    public TopicExchange cardProExchange() {
        return new TopicExchange(NotificationEventPublisher.EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationPreferencesQueue() {
        return new Queue(NOTIFICATION_PREFERENCES_QUEUE, true);
    }

    @Bean
    public Binding notificationPreferencesBinding(
            Queue notificationPreferencesQueue,
            TopicExchange cardProExchange) {
        return BindingBuilder
                .bind(notificationPreferencesQueue)
                .to(cardProExchange)
                .with(NotificationEventPublisher.ROUTING_KEY);
    }
}
