package com.cardpro.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a refresh token is expired, revoked, or otherwise invalid.
 * Maps to HTTP 401 UNAUTHORIZED with error code REFRESH_TOKEN_EXPIRED.
 */
public class TokenExpiredException extends ApiException {

    public TokenExpiredException(String message) {
        super("REFRESH_TOKEN_EXPIRED", message, HttpStatus.UNAUTHORIZED);
    }
}
