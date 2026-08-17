package com.cardpro.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a singleton {@link RazorpayClient} wired from the configured API
 * credentials ({@code app.razorpay.key-id} / {@code app.razorpay.key-secret},
 * overridable via the RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET env vars).
 */
@Configuration
public class RazorpayConfig {

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
