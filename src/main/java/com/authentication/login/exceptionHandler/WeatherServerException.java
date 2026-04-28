package com.authentication.login.exceptionHandler;

public class WeatherServerException extends RuntimeException {
    public WeatherServerException(String message) {
        super("Weather service error: " + message);
    }
}

