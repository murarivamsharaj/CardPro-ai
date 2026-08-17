package com.cardpro.payment.dto;

/**
 * Published on {@code cardpro.events.exchange} with the
 * {@code payment.completed.routing.key} after a payment is verified, so other
 * services can unlock the purchased entitlement for the user.
 */
public record PaymentCompletedEvent(String userId, String transactionId, String itemType) {}
