package com.cardpro.userservice.service;

import com.cardpro.userservice.dto.CreateOrderResponse;
import com.cardpro.userservice.dto.VerifyPaymentRequest;
import com.cardpro.userservice.dto.VerifyPaymentResponse;
import com.cardpro.userservice.entity.User;
import com.cardpro.userservice.exception.PaymentVerificationException;
import com.cardpro.userservice.exception.UserProfileNotFoundException;
import com.cardpro.userservice.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Razorpay-backed CardPro Pro upgrades.
 *
 * <p>An order is created server-side for a fixed ₹999 price; the browser then
 * opens the Razorpay Checkout modal. After payment the modal hands back
 * {@code order_id|payment_id|signature}; the signature is verified
 * cryptographically (HMAC-SHA256 with the key secret) before {@code isPro} is
 * flipped on the user — the client is never trusted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService {

    /** Fixed CardPro Pro price, in paise (₹999.00). */
    public static final int PRO_PRICE_IN_PAISE = 99900;

    public static final String PRO_CURRENCY = "INR";

    private final RazorpayClient razorpayClient;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Transactional
    public CreateOrderResponse createProOrder(String email) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", PRO_PRICE_IN_PAISE);
            orderRequest.put("currency", PRO_CURRENCY);
            orderRequest.put("receipt", "cardpro_pro_" + email.replaceAll("[^A-Za-z0-9]", "_") + "_" + System.currentTimeMillis());
            orderRequest.put("notes", new JSONObject().put("purpose", "CardPro Pro upgrade"));

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Created Razorpay order {} for user {}", order.get("id"), email);

            return CreateOrderResponse.builder()
                    .orderId((String) order.get("id"))
                    .keyId(razorpayKeyId)
                    .amount(PRO_PRICE_IN_PAISE)
                    .currency(PRO_CURRENCY)
                    .status("created")
                    .build();
        } catch (RazorpayException ex) {
            log.error("Razorpay order creation failed for user {}", email, ex);
            throw new IllegalStateException("Failed to create payment order: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public VerifyPaymentResponse verifyProPayment(String email, VerifyPaymentRequest request) {
        // Razorpay signs "order_id|payment_id" with HMAC-SHA256 using the key secret.
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

        boolean signatureValid;
        try {
            signatureValid = Utils.verifySignature(payload, request.getSignature(), razorpayKeySecret);
        } catch (Exception ex) {
            log.warn("Signature check errored for user {}", email, ex);
            throw new PaymentVerificationException("Payment signature could not be verified");
        }

        if (!signatureValid) {
            log.warn("Invalid Razorpay signature for user {}", email);
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserProfileNotFoundException(email));

        user.setPro(true);
        userRepository.save(user);
        log.info("User {} upgraded to CardPro Pro", email);

        return VerifyPaymentResponse.builder()
                .success(true)
                .message("Payment verified. CardPro Pro activated.")
                .pro(true)
                .build();
    }
}
