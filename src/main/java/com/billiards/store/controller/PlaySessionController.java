package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.play_session.AdjustStartTimeRequestDto;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.service.PlaySessionService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/play-session")
public class PlaySessionController {
    private final PlaySessionService playSessionService;

    public PlaySessionController(PlaySessionService playSessionService) {
        this.playSessionService = playSessionService;
    }


    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<?>> create(@RequestBody PlaySessionRequestDto request) {
        PlaySessionResponseDto responseDto = playSessionService.create(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                SystemLanguage.getMessage("create_success"),
                responseDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/start-time/{sessionCode}")
    public ResponseEntity<SuccessResponse<?>> adjustStartTime(@PathVariable String sessionCode, @RequestBody AdjustStartTimeRequestDto requestDto) {
        PlaySessionResponseDto responseDto = playSessionService.adjustStartTime(sessionCode, requestDto);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                SystemLanguage.getMessage("update_success"),
                responseDto,
                LocalDateTime.now()
        ));
    }
    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<PlaySessionResponseDto> sessionsDto = playSessionService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                sessionsDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        PlaySessionResponseDto responseDto = playSessionService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/session-code/{sessionCode}")
    public ResponseEntity<SuccessResponse<?>> findBySessionCode(@PathVariable String sessionCode) {
        PlaySessionResponseDto sessionDto = playSessionService.findBySessionCode(sessionCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                sessionDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/fee")
    public ResponseEntity<SuccessResponse<?>> getCurrentFee(String playSessionCode) {
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                playSessionService.calculateTableFee(playSessionCode),
                LocalDateTime.now()
        ));
    }
    

    @PutMapping("/close/{sessionCode}")
    public ResponseEntity<SuccessResponse<?>> closePlaySession(@PathVariable String sessionCode) {
        playSessionService.endPlaySession(sessionCode);
        
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("session_ended"),
                null,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/current")
    public ResponseEntity<SuccessResponse<?>> getCurrentTableSession(@RequestParam("tableCode") String tableCode) {
        PlaySessionResponseDto playSessionResponseDto = playSessionService.getCurrentSessionOfOccupiedTable(tableCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                playSessionResponseDto,
                LocalDateTime.now()
        ));
    }
    


    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable UUID id) {
        playSessionService.delete(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                null,
                LocalDateTime.now()
        ));
    }
}

