package com.authentication.login.service;

import com.authentication.login.entity.User;
import com.authentication.login.enums.Role;
import com.authentication.login.exceptionHandler.EmailAlreadyExistsException;
import com.authentication.login.exceptionHandler.InvalidPasswordException;
import com.authentication.login.exceptionHandler.UserNotFoundException;
import com.authentication.login.repository.UserRepository;
import com.authentication.login.requests.LoginRequest;
import com.authentication.login.requests.RegisterRequest;
import com.authentication.login.responses.LoginResponse;
import com.authentication.login.responses.RegisterResponse;
import com.authentication.login.server.JwtServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtServerClient jwtServerClient;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException(registerRequest.getEmail());
        }
        User saved = userRepository.save(
                User.builder()
                        .email(registerRequest.getEmail())
                        .password(registerRequest.getPassword())
                        .role(Role.USER)
                        .build()
        );
        log.info("New User registered with email {}", registerRequest.getEmail());
        return RegisterResponse.builder()
                .success(true)
                .message("Registration Successful. Welcome, " + saved.getEmail())
                .userId(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // 2. check if password is correct or not
        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidPasswordException();
        }

        // 3. Request token from jwt-server
        JwtServerClient.TokenResult tokenResult =
                jwtServerClient.generateToken(user.getEmail(), user.getPassword(), user.getRole().name());

        log.info("User logged in → id={} email={} role={}", user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .success(true)
                .message("Login successful! Welcome back, " + user.getEmail())
                .token(tokenResult.token())
                .expiresInMs(tokenResult.expiresInMs())
                .email(user.getEmail())
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public String userHello() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.getRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. This endpoint is for admins only.");
        }

        return "Hello, " + user.getEmail();
    }

    public String adminHello() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. This endpoint is for admins only.");
        }

        return "Hello, " + user.getEmail();
    }
}
