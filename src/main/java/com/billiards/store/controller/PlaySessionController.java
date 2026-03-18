package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
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
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<PlaySessionResponseDto> sessionsDto = playSessionService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                sessionsDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        PlaySessionResponseDto responseDto = playSessionService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/session-code/{sessionCode}")
    public ResponseEntity<SuccessResponse<?>> findBySessionCode(@PathVariable String sessionCode) {
        PlaySessionResponseDto sessionDto = playSessionService.findBySessionCode(sessionCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                sessionDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> update(@PathVariable UUID id, @RequestBody PlaySessionRequestDto request) {
        PlaySessionResponseDto updatedDto = playSessionService.update(id, request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                updatedDto,
                LocalDateTime.now()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable UUID id) {
        playSessionService.delete(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Play session deleted successfully",
                null,
                LocalDateTime.now()
        ));
    }
}

