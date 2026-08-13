package com.cardpro.userservice.service;

import com.cardpro.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publishes user notification-preference changes to RabbitMQ.
 *
 * <p>This is the "prep" half of the email-notifications toggle: consumers
 * (e.g. the lead-service notification pipeline) subscribe to the
 * {@code email.notifications.updated} routing key and can decide whether to
 * send new-lead emails per user without touching user-service's database.
 *
 * <p>Failures are logged and swallowed — the preference is already persisted
 * in Postgres, so the toggle must never fail because the broker is down.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    public static final String EXCHANGE = "cardpro.exchange";
    public static final String ROUTING_KEY = "email.notifications.updated";

    private final AmqpTemplate amqpTemplate;

    public void publishNotificationPreferenceChanged(User user) {
        try {
            Map<String, Object> event = Map.of(
                    "email", user.getEmail(),
                    "enabled", Boolean.TRUE.equals(user.getEmailNotificationsEnabled()),
                    "userId", user.getId() != null ? user.getId().toString() : null,
                    "type", "email.notifications.updated"
            );
            amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
            log.info("Published {} event for user {}", ROUTING_KEY, user.getEmail());
        } catch (AmqpException | IllegalStateException e) {
            log.warn("Could not publish {} event for user {} (broker unreachable?): {}",
                    ROUTING_KEY, user.getEmail(), e.getMessage());
        }
    }
}
