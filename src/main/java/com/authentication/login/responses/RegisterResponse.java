package com.authentication.login.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private boolean success;
    private String message;
    private String userId;
    private String email;
}