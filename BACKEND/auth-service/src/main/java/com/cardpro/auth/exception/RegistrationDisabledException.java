package com.cardpro.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when public self-registration has been globally disabled by an admin.
 * Maps to HTTP 403 FORBIDDEN with code REGISTRATION_DISABLED.
 */
public class RegistrationDisabledException extends ApiException {

    public RegistrationDisabledException(String message) {
        super("REGISTRATION_DISABLED", message, HttpStatus.FORBIDDEN);
    }
}
