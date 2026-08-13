package com.cardpro.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the current password supplied on a password change does not
 * match the stored hash. Maps to HTTP 400 BAD_REQUEST with code
 * PASSWORD_MISMATCH.
 */
public class PasswordMismatchException extends ApiException {

    public PasswordMismatchException(String message) {
        super("PASSWORD_MISMATCH", message, HttpStatus.BAD_REQUEST);
    }
}
