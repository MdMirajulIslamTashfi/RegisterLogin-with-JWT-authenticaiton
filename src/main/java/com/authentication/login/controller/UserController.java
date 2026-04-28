package com.authentication.login.controller;

import com.authentication.login.requests.LoginRequest;
import com.authentication.login.requests.RegisterRequest;
import com.authentication.login.responses.LoginResponse;
import com.authentication.login.responses.RegisterResponse;
import com.authentication.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<String> userHello() {
        String response = userService.userHello();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminHello() {
        String response = userService.adminHello();
        return ResponseEntity.ok(response);
    }
}
