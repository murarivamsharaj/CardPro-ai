package com.cardpro.userservice.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        // Hardcoding the test keys directly to bypass application.yml completely!
        return new RazorpayClient("rzp_test_TPi0wBlkI3xiuG", "V7jK6wf07WMMhYlUtMz1udyW");
    }
}