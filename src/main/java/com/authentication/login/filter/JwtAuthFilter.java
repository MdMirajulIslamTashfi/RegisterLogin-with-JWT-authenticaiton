package com.authentication.login.filter;

import com.authentication.login.server.JwtServerClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtServerClient jwtServerClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token → skip, Spring Security will handle the 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Ask jwt-server to validate and extract claims
        JwtServerClient.ValidateResult result = jwtServerClient.validateToken(token);

        if (result.valid() && result.role() != null) {

            // role is either "ROLE_USER" or "ROLE_ADMIN"
            // Spring Security uses this to enforce @PreAuthorize rules
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + result.role());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            result.email(),       // principal — used by authentication.getName()
                            null,
                            List.of(authority)    // granted authority based on role
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Token valid → email={} role={}", result.email(), result.role());

        } else {
            log.warn("Token invalid or missing role → {}", result.message());
            // Don't set authentication — Spring Security will block the request as 401/403
        }

        filterChain.doFilter(request, response);
    }
}

