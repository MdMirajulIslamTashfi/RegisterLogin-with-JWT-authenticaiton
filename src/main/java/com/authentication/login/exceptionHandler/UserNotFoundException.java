package com.authentication.login.exceptionHandler;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("No account found with email: " + email);
    }
}