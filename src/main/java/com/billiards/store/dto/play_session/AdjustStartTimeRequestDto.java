package com.billiards.store.dto.play_session;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdjustStartTimeRequestDto {
    private LocalDateTime startTime;
}
