package com.cardpro.userservice.exception;

/**
 * Exception thrown when attempting to create a user with an email
 * that already exists in the database.
 */
public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("A user with email '" + email + "' already exists");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
