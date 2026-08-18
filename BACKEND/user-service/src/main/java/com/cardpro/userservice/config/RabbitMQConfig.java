package com.cardpro.userservice.config;

// ❌ Disabled on Render until a dedicated RabbitMQ broker (e.g. CloudAMQP) is configured.
// @Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_PREFERENCES_QUEUE = "user.notification.preferences";

    /*
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
    */
}