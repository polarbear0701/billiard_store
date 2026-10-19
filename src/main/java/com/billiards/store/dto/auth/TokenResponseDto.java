package com.billiards.store.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Access and refresh tokens")
public class TokenResponseDto {
    @Schema(description = "JWT to send as `Authorization: Bearer <accessToken>`", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "JWT used only to get a new access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    @Schema(description = "Lifetime of the access token in milliseconds", example = "3600000")
    private long expiresIn;
}
