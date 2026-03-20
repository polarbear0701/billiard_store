package com.billiards.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.model.PlaySession;
import com.billiards.store.repository.PlaySessionRepository;

import jakarta.transaction.Transactional;

@Service
public class PlaySessionService {
    private final PlaySessionRepository playSessionRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final BilliardTableService billiardTableService;

    private static final Logger logger = LoggerFactory.getLogger(PlaySession.class);

    public PlaySessionService(
        PlaySessionRepository playSessionRepository,
        CodeGeneratorUtil codeGeneratorUtil,
        BilliardTableService billiardTableService
    ) {
        this.playSessionRepository = playSessionRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
        this.billiardTableService = billiardTableService;
    }

    @Transactional
    public PlaySessionResponseDto create(PlaySessionRequestDto requestDto) {
        PlaySession session = new PlaySession();
        session.setSessionCode(
            codeGeneratorUtil.generateCode("SES", "session_code_seq")
        );

        String tableCode = requestDto.getTableCode();

        // Validate if table code exists
        // Try catch happens is done in this function
        billiardTableService.findByTableCode(tableCode);

        session.setTableCode(tableCode);
        billiardTableService.setStatusTable(tableCode, TableStatus.OCCUPIED);

        session.setSessionStatus(requestDto.getSessionStatus());
        
        PlaySession savedSession = playSessionRepository.save(session);
        return convertToResponseDto(savedSession);
    }

    public void endPlaySession(String playSessionCode) {
        PlaySession playSession = playSessionRepository.findBySessionCodeAndIsDeletedFalse(playSessionCode)
                .orElseThrow(() -> new ResourceNotFoundException(playSessionCode));
        
        if ((playSession.getEndTime() != null) || (playSession.getSessionStatus() != SessionStatus.ACTIVE)) {
            logger.info("In here");
            throw new ResourceNotFoundException("Session is already closed or cancelled");
        }

        playSession.setEndTime(LocalDateTime.now());
        playSession.setSessionStatus(SessionStatus.COMPLETED);
        billiardTableService.setStatusTable(playSession.getTableCode(), TableStatus.AVAILABLE);
        playSessionRepository.save(playSession);
    }

    public PlaySessionResponseDto findById(UUID id) {
        PlaySession session = playSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));
        return convertToResponseDto(session);
    }

    public PlaySessionResponseDto findBySessionCode(String sessionCode) {
        PlaySession session = playSessionRepository.findBySessionCodeAndIsDeletedFalse(sessionCode)
            .orElseThrow(() -> new ResourceNotFoundException("No play session associated with this session code. Please try again"));
        return convertToResponseDto(session);
    }

    public Long calculatePlayTime(String playSessionCode) {
        PlaySession playSession = playSessionRepository.findBySessionCodeAndIsDeletedFalse(playSessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("No play session found"));
        if (playSession.getEndTime() == null || playSession.getStartTime() == null) {
            throw new ResourceNotFoundException("No endtime or start time found");
        }
        long duration = Duration.between(playSession.getStartTime(), playSession.getEndTime()).toMinutes();
        return duration;
    }

    public BigDecimal calculateTableFee(String playSessionCode) {
        Long playTime = calculatePlayTime(playSessionCode);

        PlaySession playSession = playSessionRepository.findBySessionCodeAndIsDeletedFalse(playSessionCode)
            .orElseThrow(() -> new ResourceNotFoundException("No play session found"));

        BigDecimal pricePerHour = billiardTableService.getTablePricePerHour(playSession.getTableCode());

        // 4. Calculate Fee: (Minutes / 60.0) * Price
        BigDecimal hours = BigDecimal.valueOf(playTime)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        return pricePerHour.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    public List<PlaySessionResponseDto> findAll() {
        return playSessionRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public PlaySessionResponseDto update(UUID id, PlaySessionRequestDto requestDto) {
        PlaySession session = playSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));

        session.setTableCode(requestDto.getTableCode());
        session.setSessionStatus(requestDto.getSessionStatus());
        
        PlaySession updatedSession = playSessionRepository.save(session);
        return convertToResponseDto(updatedSession);
    }

    public void delete(UUID id) {
        PlaySession session = playSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Play session not found"));
        session.setIsDeleted(true);
        playSessionRepository.save(session);
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
