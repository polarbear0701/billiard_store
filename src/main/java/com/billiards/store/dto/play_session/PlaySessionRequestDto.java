package com.billiards.store.dto.play_session;


import com.billiards.store.common.enumerated.SessionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for starting a play session")
public class PlaySessionRequestDto {
    @Schema(description = "Code of the table to play on", example = "TBL000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tableCode;
    @Schema(description = "Status of the new session, use ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private SessionStatus sessionStatus;
}
