package com.cardpro.payment.service;

import com.cardpro.payment.dto.PaymentCompletedEvent;
import com.cardpro.payment.dto.request.CreateOrderRequest;
import com.cardpro.payment.dto.request.VerifyPaymentRequest;
import com.cardpro.payment.dto.response.CreateOrderResponse;
import com.cardpro.payment.dto.response.VerifyPaymentResponse;
import com.cardpro.payment.entity.Transaction;
import com.cardpro.payment.enums.ItemType;
import com.cardpro.payment.enums.TransactionStatus;
import com.cardpro.payment.repository.TransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final RazorpayClient razorpayClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.currency}")
    private String currency;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    public CreateOrderResponse createOrder(String userId, CreateOrderRequest request) {

        // Null-safe price calculation
        int amountRupees;
        if (request.getAmount() != null) {
            amountRupees = request.getAmount();
        } else if (request.getItemType() != null) {
            amountRupees = getPriceForItem(request.getItemType()).intValue();
        } else {
            amountRupees = 999; // Default fallback for an unspecified Pro Upgrade
        }

        String receiptId = request.getReceiptId() != null && !request.getReceiptId().isBlank()
                ? request.getReceiptId()
                : "cardpro_" + UUID.randomUUID().toString().substring(0, 8);

        String rzpOrderId = createRazorpayOrder(amountRupees, receiptId);

        Transaction transaction = Transaction.builder()
                .userId(UUID.fromString(userId))
                .itemType(request.getItemType()) // Can safely be null for custom amounts
                .amount(BigDecimal.valueOf(amountRupees))
                .rzpOrderId(rzpOrderId)
                .status(TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        return CreateOrderResponse.builder()
                .orderId(rzpOrderId)
                .razorpayKeyId(razorpayKeyId)
                .amount(amountRupees * 100)
                .currency(currency)
                .status("created")
                .build();
    }

    /**
     * Creates a Razorpay order for the given amount (in rupees) and receipt
     * reference. The amount is converted to paise ({@code amount * 100}) as
     * required by the Razorpay API, and the generated order id is returned.
     */
    public String createRazorpayOrder(Integer amountInRupees, String receiptId) {
        try {
            int amountInPaise = amountInRupees * 100;

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException | org.json.JSONException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public VerifyPaymentResponse verifyPayment(String userId, VerifyPaymentRequest request) {
        Transaction transaction = transactionRepository.findByRzpOrderId(request.getRazorpayOrderId())
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Cryptographically verify the Razorpay signature before marking the
        // transaction SUCCESS so forged payment ids can never unlock a purchase.
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.getRazorpayOrderId());
        options.put("razorpay_payment_id", request.getRazorpayPaymentId());
        options.put("razorpay_signature", request.getSignature());

        boolean isValid;
        try {
            isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (RazorpayException e) {
            log.error("Razorpay signature verification errored: {}", e.getMessage(), e);
            throw new IllegalStateException("Payment signature verification failed", e);
        }

        if (!isValid) {
            log.warn("Rejected payment with invalid signature for order {}", request.getRazorpayOrderId());
            throw new IllegalStateException("Payment signature verification failed. Possible tampering detected.");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRzpPaymentId(request.getRazorpayPaymentId());
        transactionRepository.save(transaction);

        // Broadcast so other services can unlock the purchased entitlement.
        rabbitTemplate.convertAndSend(
            "cardpro.events.exchange",
            "payment.completed.routing.key",
            new PaymentCompletedEvent(
                transaction.getUserId().toString(),
                transaction.getId().toString(),
                transaction.getItemType().name())
        );
        log.info("Published payment.completed event for transaction {}", transaction.getId());

        return VerifyPaymentResponse.builder()
            .success(true)
            .message("Payment verified successfully")
            .build();
    }

    public Page<?> getTransactionHistory(String userId, int page, int size) {
        return transactionRepository.findByUserId(
            UUID.fromString(userId),
            PageRequest.of(page, size)
        );
    }

    private BigDecimal getPriceForItem(ItemType itemType) {
        return switch (itemType) {
            case TEMPLATE -> BigDecimal.valueOf(149);
            case NFC -> BigDecimal.valueOf(999);
            case CUSTOM_DOMAIN -> BigDecimal.valueOf(499);
            case LEAD_PACK -> BigDecimal.valueOf(199);
            case AI_PHOTO -> BigDecimal.valueOf(49);
        };
    }
}
