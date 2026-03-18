package com.billiards.store.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.model.PlaySession;
import com.billiards.store.repository.PlaySessionRepository;

@Service
public class PlaySessionService {
    private final PlaySessionRepository playSessionRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;

    public PlaySessionService(
        PlaySessionRepository playSessionRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.playSessionRepository = playSessionRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    public PlaySessionResponseDto create(PlaySessionRequestDto requestDto) {
        PlaySession session = new PlaySession();
        session.setSessionCode(
            codeGeneratorUtil.generateCode("SES", "play_session_code_seq")
        );
        session.setTableCode(requestDto.getTableCode());
        session.setEndTime(requestDto.getEndTime());
        session.setSessionStatus(requestDto.getSessionStatus());
        
        PlaySession savedSession = playSessionRepository.save(session);
        return convertToResponseDto(savedSession);
    }

    public PlaySessionResponseDto findById(UUID id) {
        PlaySession session = playSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));
        return convertToResponseDto(session);
    }

    public PlaySessionResponseDto findBySessionCode(String sessionCode) {
        PlaySession session = playSessionRepository.findBySessionCode(sessionCode)
            .orElseThrow(() -> new ResourceNotFoundException("No play session associated with this session code. Please try again"));
        return convertToResponseDto(session);
    }

    public List<PlaySessionResponseDto> findAll() {
        return playSessionRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public PlaySessionResponseDto update(UUID id, PlaySessionRequestDto requestDto) {
        PlaySession session = playSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));

        session.setTableCode(requestDto.getTableCode());
        session.setEndTime(requestDto.getEndTime());
        session.setSessionStatus(requestDto.getSessionStatus());
        
        PlaySession updatedSession = playSessionRepository.save(session);
        return convertToResponseDto(updatedSession);
    }

    public void delete(UUID id) {
        playSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));
        playSessionRepository.deleteById(id);
    }

    private PlaySessionResponseDto convertToResponseDto(PlaySession session) {
        PlaySessionResponseDto dto = new PlaySessionResponseDto();
        dto.setSessionCode(session.getSessionCode());
        dto.setTableCode(session.getTableCode());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setSessionStatus(session.getSessionStatus());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        dto.setCreatedBy(session.getCreatedBy());
        dto.setUpdatedBy(session.getUpdatedBy());
        return dto;
    }
}
