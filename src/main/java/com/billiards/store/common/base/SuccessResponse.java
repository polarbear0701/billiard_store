package com.billiards.store.common.base;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Envelope of every successful response: the result is in `data`")
public class SuccessResponse <T> {
    @Schema(description = "Status code repeated in the body. Usually the HTTP status, but some create and update endpoints send 202 here while the HTTP status is 200: rely on the HTTP status", example = "200")
    private int code;
    @Schema(description = "Short human-readable message, in the server's language (Vietnamese by default). For display only: do not parse it", example = "Thành công")
    private String message;
    @Schema(description = "The result. Its shape depends on the endpoint; null when the endpoint returns nothing")
    private T data;
    @Schema(description = "When the response was created (ISO-8601 date-time in the store's timezone)", example = "2026-10-19T18:30:00")
    private LocalDateTime timestamp;
}
