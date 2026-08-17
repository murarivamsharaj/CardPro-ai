package com.cardpro.card.dto.event;

/**
 * Consumed from {@code cardpro.events.exchange} (routing key
 * {@code payment.completed.#}) after a payment is verified in payment-service.
 * Lets card-service unlock premium features for the paying user.
 */
public record PaymentCompletedEvent(String userId, String transactionId, String itemType) {}
