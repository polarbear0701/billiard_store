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
    public PlaySessionResponseDto create(@RequestBody PlaySessionRequestDto request) {
        return playSessionService.create(request);
    }

    @GetMapping
    public List<PlaySessionResponseDto> findAll() {
        return playSessionService.findAll();
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
    public PlaySessionResponseDto findBySessionCode(@PathVariable String sessionCode) {
        return playSessionService.findBySessionCode(sessionCode);
    }

    @PutMapping("/{id}")
    public PlaySessionResponseDto update(@PathVariable UUID id, @RequestBody PlaySessionRequestDto request) {
        return playSessionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        playSessionService.delete(id);
    }
}

