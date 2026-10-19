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
@Schema(description = "Result of checking a refresh token")
public class ValidateTokenResponseDto {
    @Schema(description = "true if the refresh token can still be used", example = "true")
    private boolean valid;
    @Schema(description = "Account the token belongs to; null when invalid", example = "admin")
    private String username;
    @Schema(description = "Remaining lifetime of the token in milliseconds; 0 when invalid", example = "86400000")
    private long expiresIn;
}
