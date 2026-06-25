package com.billiards.store.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponseDto {
    private boolean valid;
    private String username;
    private long expiresIn;
}
