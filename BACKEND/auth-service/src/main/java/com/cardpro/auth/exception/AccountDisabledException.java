package com.cardpro.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a soft-deleted/disabled account attempts to log in or refresh
 * its tokens. Maps to HTTP 403 FORBIDDEN with code ACCOUNT_DISABLED.
 */
public class AccountDisabledException extends ApiException {

    public AccountDisabledException(String message) {
        super("ACCOUNT_DISABLED", message, HttpStatus.FORBIDDEN);
    }
}
