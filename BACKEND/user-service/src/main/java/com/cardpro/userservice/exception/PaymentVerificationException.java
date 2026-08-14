package com.cardpro.userservice.exception;

/** Thrown when a Razorpay payment signature fails cryptographic verification. */
public class PaymentVerificationException extends RuntimeException {

    public PaymentVerificationException(String message) {
        super(message);
    }
}
