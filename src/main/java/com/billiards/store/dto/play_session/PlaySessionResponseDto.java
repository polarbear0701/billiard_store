package com.billiards.store.dto.play_session;

import java.time.LocalDateTime;

import com.billiards.store.common.enumerated.SessionStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaySessionResponseDto {
    private String sessionCode;
    private String tableCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus sessionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
