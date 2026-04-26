package com.authentication.login.server;

import com.authentication.login.exceptionHandler.JwtServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtServerClient {
    private final RestTemplate restTemplate;

    @Value("${jwt.server.generate-url}")
    public String generateJwtUrl;

    @Value("${jwt.server.validate-url}")
    public String validateJwtUrl;

    // asking jwtServer to generate token
    public TokenResult generateToken(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = Map.of("email", email, "password", password);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(generateJwtUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object token = response.getBody().get("token");
                long expiresInMs = getLong(response.getBody(), "expiresInMs");
                if (token != null) return new TokenResult(token.toString(), expiresInMs);
                throw new JwtServerException("jwt-server response missing 'token' field");
            }
            throw new JwtServerException("jwt-server returned status: " + response.getStatusCode());
        } catch (Exception ex) {
            log.error("Failed to generate token from jwt-server: {}", ex.getMessage());
            throw new JwtServerException("Token generation failed: " + ex.getMessage());
        }
    }

    private long getLong(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0L;
        return ((Number) val).longValue();
    }

    // Records returned to AuthService
    public record TokenResult(String token, long expiresInMs) {
    }
}
