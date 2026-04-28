package com.authentication.login.responses;

import com.authentication.login.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private long expiresInMs;
    private String email;
    private String userId;
    private Role role;
}
