package com.billiards.store.common.base;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Body of every error response (400, 401, 403, 404, 500)")
public class ErrorResponse {
    @Schema(description = "HTTP status code of the error", example = "404")
    private int status;
    @Schema(description = "What went wrong, in the server's language (Vietnamese by default). For display only: do not parse it", example = "Không tìm thấy dữ liệu")
    private String message;
    @Schema(description = "When the error happened (ISO-8601 date-time in the store's timezone)", example = "2026-10-19T18:30:00")
    private LocalDateTime timestamp;
}
