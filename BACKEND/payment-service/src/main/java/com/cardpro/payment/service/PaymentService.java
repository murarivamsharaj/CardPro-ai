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

    @Value("${app.razorpay.key-id:rzp_test_TQnTopgwwCYbNN}")
    private String razorpayKeyId;

    @Value("${app.razorpay.currency:INR}")
    private String currency;

    @Value("${app.razorpay.key-secret:9mEw4VScMpxSv7qdqHngi0D8}")
    private String razorpayKeySecret;

    public CreateOrderResponse createOrder(String userId, CreateOrderRequest request) {
        int amountRupees = 999;
        ItemType itemType = null;
        String receiptId = "cardpro_" + UUID.randomUUID().toString().substring(0, 8);

        if (request != null) {
            if (request.getAmount() != null && request.getAmount() > 0) {
                amountRupees = request.getAmount();
            } else if (request.getItemType() != null) {
                amountRupees = getPriceForItem(request.getItemType()).intValue();
            }
            if (request.getItemType() != null) {
                itemType = request.getItemType();
            }
            if (request.getReceiptId() != null && !request.getReceiptId().isBlank()) {
                receiptId = request.getReceiptId();
            }
        }

        // 1. Create order on Razorpay
        String rzpOrderId = createRazorpayOrder(amountRupees, receiptId);

        // 2. Safely attempt database record creation (non-blocking for demo resilience)
        try {
            Transaction transaction = Transaction.builder()
                    .userId(UUID.fromString(userId))
                    .itemType(itemType)
                    .amount(BigDecimal.valueOf(amountRupees))
                    .rzpOrderId(rzpOrderId)
                    .status(TransactionStatus.PENDING)
                    .build();

            transactionRepository.save(transaction);
        } catch (Exception e) {
            log.warn("Database transaction record skipped or failed: {}", e.getMessage());
        }

        return CreateOrderResponse.builder()
                .orderId(rzpOrderId)
                .razorpayKeyId(razorpayKeyId != null ? razorpayKeyId : "rzp_test_TQnTopgwwCYbNN")
                .amount(amountRupees * 100)
                .currency(currency != null ? currency : "INR")
                .status("created")
                .build();
    }

    public String createRazorpayOrder(Integer amountInRupees, String receiptId) {
        try {
            int amountInPaise = amountInRupees * 100;
            String activeCurrency = (currency != null && !currency.isBlank()) ? currency : "INR";

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", activeCurrency);
            orderRequest.put("receipt", receiptId);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException | org.json.JSONException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public VerifyPaymentResponse verifyPayment(String userId, VerifyPaymentRequest request) {
        // 1. Verify Razorpay cryptographic signature
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.getRazorpayOrderId());
        options.put("razorpay_payment_id", request.getRazorpayPaymentId());
        options.put("razorpay_signature", request.getSignature());

        boolean isValid;
        try {
            isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (RazorpayException e) {
            log.error("Razorpay signature verification error: {}", e.getMessage(), e);
            throw new IllegalStateException("Payment signature verification failed", e);
        }

        if (!isValid) {
            log.warn("Rejected payment with invalid signature for order {}", request.getRazorpayOrderId());
            throw new IllegalStateException("Payment signature verification failed. Possible tampering detected.");
        }

        // 2. Safely update transaction status in database
        try {
            transactionRepository.findByRzpOrderId(request.getRazorpayOrderId()).ifPresent(transaction -> {
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setRzpPaymentId(request.getRazorpayPaymentId());
                transactionRepository.save(transaction);
            });
        } catch (Exception e) {
            log.warn("Database status update skipped: {}", e.getMessage());
        }

        // 3. Safely broadcast message broker event (won't crash if RabbitMQ is offline)
        try {
            rabbitTemplate.convertAndSend(
                    "cardpro.events.exchange",
                    "payment.completed.routing.key",
                    new PaymentCompletedEvent(
                            userId,
                            request.getRazorpayPaymentId(),
                            "PRO_SUBSCRIPTION"
                    )
            );
            log.info("Published payment.completed event for order {}", request.getRazorpayOrderId());
        } catch (Exception e) {
            log.warn("RabbitMQ broadcast skipped: {}", e.getMessage());
        }

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
        if (itemType == null) {
            return BigDecimal.valueOf(999);
        }
        return switch (itemType) {
            case TEMPLATE -> BigDecimal.valueOf(149);
            case NFC -> BigDecimal.valueOf(999);
            case CUSTOM_DOMAIN -> BigDecimal.valueOf(499);
            case LEAD_PACK -> BigDecimal.valueOf(199);
            case AI_PHOTO -> BigDecimal.valueOf(49);
        };
    }
}