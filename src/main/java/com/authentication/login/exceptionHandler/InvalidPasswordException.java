package com.authentication.login.exceptionHandler;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("The password you entered is incorrect");
    }
}