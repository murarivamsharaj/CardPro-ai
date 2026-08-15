package com.cardpro.userservice.service;

import com.cardpro.userservice.dto.CreateOrderResponse;
import com.cardpro.userservice.dto.VerifyPaymentRequest;
import com.cardpro.userservice.dto.VerifyPaymentResponse;
import com.cardpro.userservice.entity.User;
import com.cardpro.userservice.exception.PaymentVerificationException;
import com.cardpro.userservice.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Razorpay-backed CardPro Pro upgrades.
 *
 * <p>An order is created server-side for a fixed ₹999 price; the browser then
 * opens the Razorpay Checkout modal. After payment the modal hands back
 * {@code order_id|payment_id|signature}, which is verified cryptographically
 * (HMAC-SHA256 via {@link Utils#verifyPaymentSignature}) before {@code pro}
 * is flipped on the user — the client is never trusted.
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

    // Hardcoded keys to ensure signature verification matches order creation
    private final String razorpayKeyId = "rzp_test_TPi0wBlkI3xiuG";
    private final String razorpayKeySecret = "V7jK6wf07WMMhYlUtMz1udyW";

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
        // Real signature verification — the browser hands back order_id,
        // payment_id and signature; a valid signature proves Razorpay itself
        // confirmed the payment. This was previously bypassed, which let any
        // caller activate Pro for free.
        boolean validSignature;
        try {
            JSONObject paymentAttributes = new JSONObject()
                    .put("razorpay_order_id", request.getRazorpayOrderId())
                    .put("razorpay_payment_id", request.getRazorpayPaymentId())
                    .put("razorpay_signature", request.getSignature());
            validSignature = Utils.verifyPaymentSignature(paymentAttributes, razorpayKeySecret);
        } catch (RazorpayException ex) {
            log.warn("Signature verification errored for order {} (user {})", request.getRazorpayOrderId(), email, ex);
            throw new PaymentVerificationException("Payment signature verification failed. Please contact support with your payment ID.");
        }
        if (!validSignature) {
            log.warn("Signature verification failed for order {} (user {})", request.getRazorpayOrderId(), email);
            throw new PaymentVerificationException("Payment signature verification failed. Please contact support with your payment ID.");
        }

        // Ghost profile: auth-service owns the account, but user-service's own
        // row may not exist yet. Build one on the fly with ALL NOT NULL columns
        // (firstName/lastName/role) so the save below cannot violate the schema.
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.warn("Ghost profile detected for {}. Creating fresh database record on the fly.", email);
            String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            return User.builder()
                    .email(email)
                    .firstName(localPart.isEmpty() ? "User" : localPart)
                    .lastName("")
                    .displayName(localPart.isEmpty() ? "User" : localPart)
                    .role("ROLE_USER")
                    .active(true)
                    .emailNotificationsEnabled(true)
                    .build();
        });

        // Force the user to PRO status
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