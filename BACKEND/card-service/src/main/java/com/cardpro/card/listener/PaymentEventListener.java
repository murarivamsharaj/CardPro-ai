package com.cardpro.card.listener;

import com.cardpro.card.config.RabbitMQConfig;
import com.cardpro.card.dto.event.PaymentCompletedEvent;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Consumes {@code payment.completed} events from payment-service and unlocks
 * the purchased premium features for the user (e.g. premium templates).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final CardProfileRepository cardProfileRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_EVENTS_QUEUE)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event == null || event.userId() == null || event.itemType() == null) {
            log.warn("Ignoring payment.completed event with missing fields");
            return;
        }

        log.info("Received payment.completed event: userId={}, transactionId={}, itemType={}",
                event.userId(), event.transactionId(), event.itemType());

        switch (event.itemType()) {
            case "TEMPLATE" -> unlockPremiumTemplates(event.userId());
            case "LEAD_PACK" -> addLeadCredits(event.userId());
            case "NFC", "CUSTOM_DOMAIN", "AI_PHOTO" ->
                    log.info("No unlock logic for itemType '{}' yet (user {})", event.itemType(), event.userId());
            default -> log.warn("Unknown itemType '{}' in payment.completed event", event.itemType());
        }
    }

    private void unlockPremiumTemplates(String userId) {
        // Changed findByUserId to findFirstByUserId to handle multiple cards safely
        Optional<CardProfile> profile = cardProfileRepository.findFirstByUserId(UUID.fromString(userId));
        if (profile.isEmpty()) {
            log.warn("No card profile found for user {}, cannot unlock premium templates", userId);
            return;
        }

        profile.get().setPremiumTemplatesUnlocked(true);
        cardProfileRepository.save(profile.get());
        log.info("Unlocking Premium Templates for user: {}", userId);
    }

    private void addLeadCredits(String userId) {
        // Changed findByUserId to findFirstByUserId to handle multiple cards safely
        Optional<CardProfile> profile = cardProfileRepository.findFirstByUserId(UUID.fromString(userId));
        if (profile.isEmpty()) {
            log.warn("No card profile found for user {}, cannot add lead credits", userId);
            return;
        }

        CardProfile cardProfile = profile.get();
        cardProfile.setLeadCredits(cardProfile.getLeadCredits() + 100);
        cardProfileRepository.save(cardProfile);
        log.info("Added 100 Lead Credits for user: {} New balance: {}", userId, cardProfile.getLeadCredits());
    }
}