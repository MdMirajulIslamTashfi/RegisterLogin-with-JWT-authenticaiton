package com.authentication.login.service;

import com.authentication.login.entity.User;
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
import org.springframework.stereotype.Service;

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
                        .build()
        );
        log.info("New User registered with email {}", registerRequest.getEmail());
        return RegisterResponse.builder()
                .success(true)
                .message("Registration Successful. Welcome, " + saved.getEmail())
                .userId(saved.getId())
                .email(saved.getEmail())
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
                jwtServerClient.generateToken(user.getEmail(), user.getPassword());

        log.info("User logged in → id={} email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .success(true)
                .message("Login successful! Welcome back, " + user.getEmail())
                .token(tokenResult.token())
                .expiresInMs(tokenResult.expiresInMs())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }
}
