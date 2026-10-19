package com.billiards.store.dto.play_session;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "New start time for a play session")
public class AdjustStartTimeRequestDto {
    @Schema(description = "The corrected start time (ISO-8601 date-time in the store's timezone); must not be in the future", example = "2026-10-19T18:15:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;
}
