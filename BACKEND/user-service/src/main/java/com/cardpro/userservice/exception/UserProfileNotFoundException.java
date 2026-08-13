package com.cardpro.userservice.exception;

/**
 * Thrown when no profile record exists for the authenticated user's email
 * (e.g. the profile was never bootstrapped). Maps to 404 USER_PROFILE_NOT_FOUND
 * so the frontend can present empty defaults instead of an error.
 */
public class UserProfileNotFoundException extends RuntimeException {

    private final String email;

    public UserProfileNotFoundException(String email) {
        super("No profile found for user with email: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
