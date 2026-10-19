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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.JwtUtils;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.auth.LoginRequestDto;
import com.billiards.store.dto.auth.RefreshTokenRequestDto;
import com.billiards.store.dto.auth.TokenResponseDto;
import com.billiards.store.dto.auth.ValidateTokenResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@SecurityRequirements
@Tag(
    name = "Auth",
    description = "Login and token refresh. These are the only endpoints that do not need a bearer token."
)
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

    @Operation(
        operationId = "auth_login",
        summary = "Log in",
        description = "Exchanges a username and password for an access token and a refresh token. "
            + "Send the access token as `Authorization: Bearer <accessToken>` on every other request. "
            + "Accounts are configured on the server (there is no sign-up). Access: public."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logged in; `data` holds the tokens"),
        @ApiResponse(responseCode = "401", description = "Wrong username or password. Body is a SuccessResponse with `code` 401 and `data` null")
    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<TokenResponseDto>> login(@RequestBody LoginRequestDto loginRequest) {
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

    @Operation(
        operationId = "auth_refresh",
        summary = "Get a new access token",
        description = "Exchanges a valid refresh token for a new access token and a new refresh token. "
            + "Call it when the access token has expired (HTTP 401 on a normal request) or shortly before `expiresIn` runs out. Access: public."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New tokens in `data`"),
        @ApiResponse(responseCode = "401", description = "Refresh token missing, invalid or expired: log in again. Body is a SuccessResponse with `code` 401 and `data` null")
    })
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<TokenResponseDto>> refreshToken(
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

    @Operation(
        operationId = "auth_validate",
        summary = "Check a refresh token",
        description = "Tells whether a refresh token is still usable, for example to decide at app start whether the user must log in again. "
            + "The request body is the raw refresh token string (not a JSON object). Always returns HTTP 200; read `data.valid`. Access: public."
    )
    @PostMapping("/validate")
    public ResponseEntity<SuccessResponse<ValidateTokenResponseDto>> validateRefreshToken(
        @RequestBody String refreshToken
    ) {
        if (!jwtUtils.validateJwtToken(refreshToken, "REFRESH")) {
       		logger.warn("Token type is not REFRESH");
            return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Token validation result",
                new ValidateTokenResponseDto(false, null, 0),
                LocalDateTime.now()
            ));
        }

        logger.info("Token type is REFRESH");

        try {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            long expiresIn = Math.max(jwtUtils.getRemainingExpirationMs(refreshToken), 0);
            ValidateTokenResponseDto result = new ValidateTokenResponseDto(
                expiresIn > 0,
                username,
                expiresIn
            );

            SuccessResponse<ValidateTokenResponseDto> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                expiresIn > 0 ? "Token is valid" : "Token is invalid",
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
