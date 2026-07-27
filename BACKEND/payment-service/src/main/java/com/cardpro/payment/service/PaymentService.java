package com.cardpro.payment.service;

import com.cardpro.payment.dto.request.CreateOrderRequest;
import com.cardpro.payment.dto.request.VerifyPaymentRequest;
import com.cardpro.payment.dto.response.CreateOrderResponse;
import com.cardpro.payment.dto.response.VerifyPaymentResponse;
import com.cardpro.payment.entity.Transaction;
import com.cardpro.payment.enums.ItemType;
import com.cardpro.payment.enums.TransactionStatus;
import com.cardpro.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;

    public CreateOrderResponse createOrder(String userId, CreateOrderRequest request) {
        // Integrate with Razorpay SDK to create order
        Transaction transaction = Transaction.builder()
            .userId(UUID.fromString(userId))
            .itemType(request.getItemType())
            .amount(getPriceForItem(request.getItemType()))
            .status(TransactionStatus.PENDING)
            .build();

        transactionRepository.save(transaction);

        return CreateOrderResponse.builder()
            .orderId(transaction.getId().toString())
            .amount(getPriceForItem(request.getItemType()).multiply(BigDecimal.valueOf(100)).intValue())
            .currency("INR")
            .status("created")
            .build();
    }

    public VerifyPaymentResponse verifyPayment(String userId, VerifyPaymentRequest request) {
        // Verify Razorpay signature
        Transaction transaction = transactionRepository.findByRzpOrderId(request.getRazorpayOrderId())
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRzpPaymentId(request.getRazorpayPaymentId());
        transactionRepository.save(transaction);

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
