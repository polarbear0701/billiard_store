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
public class PlaySessionRequestDto {
    private String tableCode;
    private SessionStatus sessionStatus;
}
