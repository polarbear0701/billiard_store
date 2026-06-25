package com.billiards.store.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.JwtUtils;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.auth.LoginRequestDto;
import com.billiards.store.dto.auth.RefreshTokenRequestDto;
import com.billiards.store.dto.auth.TokenResponseDto;
import com.billiards.store.dto.auth.ValidateTokenResponseDto;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtUtils jwtUtils,
        UserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<?>> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);
            long expiresIn = jwtUtils.getAccessTokenExpirationMs();

            TokenResponseDto tokenResponse = new TokenResponseDto(
                accessToken,
                refreshToken,
                expiresIn
            );

            SuccessResponse<TokenResponseDto> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("login_success"),
                tokenResponse,
                LocalDateTime.now()
            );
            logger.info("User {} logged in successfully", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user: {} - Invalid credentials", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SuccessResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password",
                    null,
                    LocalDateTime.now()
                ));
        } catch (Exception e) {
            logger.error("Login error for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SuccessResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Login failed",
                    null,
                    LocalDateTime.now()
                ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<?>> refreshToken(
        @RequestBody RefreshTokenRequestDto request
    ) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || !jwtUtils.validateJwtToken(refreshToken, "REFRESH")) {
            logger.warn("Refresh token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SuccessResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid or expired refresh token",
                    null,
                    LocalDateTime.now()
                ));
        }

        try {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );

            String newAccessToken = jwtUtils.generateAccessToken(authentication);
            String newRefreshToken = jwtUtils.generateRefreshToken(authentication);
            long expiresIn = jwtUtils.getAccessTokenExpirationMs();

            TokenResponseDto tokenResponse = new TokenResponseDto(
                newAccessToken,
                newRefreshToken,
                expiresIn
            );

            SuccessResponse<TokenResponseDto> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token refreshed successfully",
                tokenResponse,
                LocalDateTime.now()
            );
            logger.info("Token refreshed for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SuccessResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Failed to refresh token",
                    null,
                    LocalDateTime.now()
                ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<SuccessResponse<?>> validateToken(
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token validation result",
                new ValidateTokenResponseDto(false, null, 0),
                LocalDateTime.now()
            ));
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateJwtToken(token, "ACCESS")) {
            return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token validation result",
                new ValidateTokenResponseDto(false, null, 0),
                LocalDateTime.now()
            ));
        }

        try {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            long expiresIn = Math.max(jwtUtils.getRemainingExpirationMs(token), 0);

            ValidateTokenResponseDto result = new ValidateTokenResponseDto(
                true,
                username,
                expiresIn
            );

            SuccessResponse<ValidateTokenResponseDto> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token is valid",
                result,
                LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token validation result",
                new ValidateTokenResponseDto(false, null, 0),
                LocalDateTime.now()
            ));
        }
    }
}
