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
@Schema(description = "Credentials for logging in")
public class LoginRequestDto {
    @Schema(description = "Account username", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    public String username;
    @Schema(description = "Account password", example = "your-password", requiredMode = Schema.RequiredMode.REQUIRED)
    public String password;
}
