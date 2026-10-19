package com.billiards.store.service;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.play_session.AdjustStartTimeRequestDto;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.model.PlaySession;
import com.billiards.store.repository.PlaySessionRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlaySessionService {

	private final PlaySessionRepository playSessionRepository;
	private final CodeGeneratorUtil codeGeneratorUtil;
	private final BilliardTableService billiardTableService;
	private final AuditService auditService;

	private static final String AUDIT_ENTITY = "PlaySession";

	private static final Logger logger = LoggerFactory.getLogger(
		PlaySession.class
	);

	/**
	 * Creates a new play session service.
	 *
	 * @param playSessionRepository repository for play session persistence
	 * @param codeGeneratorUtil utility for generating session codes
	 * @param billiardTableService service for table validation and status updates
	 * @param auditService service for recording audit log entries
	 */
	public PlaySessionService(
		PlaySessionRepository playSessionRepository,
		CodeGeneratorUtil codeGeneratorUtil,
		BilliardTableService billiardTableService,
		AuditService auditService
	) {
		this.playSessionRepository = playSessionRepository;
		this.codeGeneratorUtil = codeGeneratorUtil;
		this.billiardTableService = billiardTableService;
		this.auditService = auditService;
	}

	/**
	 * Creates a new active play session for a table.
	 *
	 * @param requestDto payload containing table code and initial session status
	 * @return created play session response
	 */
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
		session.setStartTime(LocalDateTime.now());

		PlaySession savedSession = playSessionRepository.save(session);
		PlaySessionResponseDto result = convertToResponseDto(savedSession);
		auditService.record(
			"CREATE",
			AUDIT_ENTITY,
			savedSession.getSessionCode(),
			null,
			result
		);
		return result;
	}

	/**
	 * Adjusts the start time of an existing session.
	 *
	 * @param playSessionCode session code to update
	 * @param newStartTimeRequestDto payload containing new start time
	 * @return updated play session response
	 */
	@Transactional
	public PlaySessionResponseDto adjustStartTime(
		String playSessionCode,
		AdjustStartTimeRequestDto newStartTimeRequestDto
	) {
		PlaySession playSession = playSessionRepository
			.findBySessionCodeAndIsDeletedFalse(playSessionCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("session_not_found")
				)
			);
		if (playSession.getSessionStatus() == SessionStatus.COMPLETED) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_completed")
			);
		}

		if (
			newStartTimeRequestDto.getStartTime().isAfter(LocalDateTime.now())
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("start_time_future")
			);
		}

		PlaySessionResponseDto before = convertToResponseDto(playSession);
		playSession.setStartTime(newStartTimeRequestDto.getStartTime());
		PlaySession savedSession = playSessionRepository.save(playSession);
		PlaySessionResponseDto result = convertToResponseDto(savedSession);
		auditService.record(
			"ADJUST_START_TIME",
			AUDIT_ENTITY,
			playSessionCode,
			before,
			result
		);
		return result;
	}

	/**
	 * Ends an active play session and releases the associated table.
	 *
	 * @param playSessionCode session code to close
	 */
	@Transactional
	public void endPlaySession(String playSessionCode) {
		PlaySession playSession = playSessionRepository
			.findBySessionCodeAndIsDeletedFalse(playSessionCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("session_not_found")
				)
			);

		if (
			playSession.getEndTime() != null ||
			playSession.getSessionStatus() != SessionStatus.ACTIVE
		) {
			logger.info("In here");
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_closed")
			);
		}

		PlaySessionResponseDto before = convertToResponseDto(playSession);
		playSession.setEndTime(LocalDateTime.now());
		playSession.setSessionStatus(SessionStatus.COMPLETED);
		billiardTableService.setStatusTable(
			playSession.getTableCode(),
			TableStatus.AVAILABLE
		);
		playSessionRepository.save(playSession);
		auditService.record(
			"END",
			AUDIT_ENTITY,
			playSessionCode,
			before,
			convertToResponseDto(playSession)
		);
	}

	/**
	 * Finds a play session by internal identifier.
	 *
	 * @param id session id
	 * @return matching play session response
	 */
	public PlaySessionResponseDto findById(UUID id) {
		PlaySession session = playSessionRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("session_not_found")
				)
			);
		return convertToResponseDto(session);
	}

	/**
	 * Finds a play session by session code.
	 *
	 * @param sessionCode business session code
	 * @return matching play session response
	 */
	public PlaySessionResponseDto findBySessionCode(String sessionCode) {
		PlaySession session = playSessionRepository
			.findBySessionCodeAndIsDeletedFalse(sessionCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("session_not_found")
				)
			);
		return convertToResponseDto(session);
	}

	/**
	 * Calculates play duration in minutes.
	 *
	 * @param session session to evaluate
	 * @return duration in minutes from start to end, or from start to now if still active
	 */
	public Long calculatePlayTime(PlaySession session) {
		if (session.getStartTime() == null) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("no_start_time")
			);
		}

		if (session.getEndTime() == null) {
			return Duration.between(
				session.getStartTime(),
				LocalDateTime.now()
			).toMinutes();
		}
		long duration = Duration.between(
			session.getStartTime(),
			session.getEndTime()
		).toMinutes();
		return duration;
	}

	/**
	 * Calculates table usage fee for a play session.
	 *
	 * @param playSessionCode session code used to fetch play data
	 * @return total table fee rounded to two decimal places
	 */
	public BigDecimal calculateTableFee(String playSessionCode) {
		PlaySession playSession = playSessionRepository
			.findBySessionCodeAndIsDeletedFalse(playSessionCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("session_not_found")
				)
			);

		Long playTime = calculatePlayTime(playSession);

		BigDecimal pricePerHour = billiardTableService.getTablePricePerHour(
			playSession.getTableCode()
		);

		// 4. Calculate Fee: (Minutes / 60.0) * Price
		BigDecimal hours = BigDecimal.valueOf(playTime).divide(
			BigDecimal.valueOf(60),
			2,
			RoundingMode.HALF_UP
		);

		return pricePerHour.multiply(hours).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Gets the currently active session of an occupied table.
	 *
	 * @param tableCode table code
	 * @return current active play session response
	 */
	public PlaySessionResponseDto getCurrentSessionOfOccupiedTable(
		String tableCode
	) {
		if (
			billiardTableService.getTableStatus(tableCode) !=
			TableStatus.OCCUPIED
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("table_not_in_session")
			);
		}

		PlaySession playSession = playSessionRepository
			.findByTableCodeAndSessionStatusAndIsDeletedFalse(
				tableCode,
				SessionStatus.ACTIVE
			)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("table_no_session")
				)
			);
		return convertToResponseDto(playSession);
	}

	/**
	 * Retrieves all non-deleted play sessions.
	 *
	 * @return list of play session responses
	 */
	public List<PlaySessionResponseDto> findAll() {
		return playSessionRepository
			.findByIsDeletedFalse()
			.stream()
			.map(this::convertToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieves a page of non-deleted play sessions.
	 *
	 * @param pageable the pagination and sorting information
	 * @return a paginated response of play session responses
	 */
	public PagedResponse<PlaySessionResponseDto> findAll(Pageable pageable) {
		return PagedResponse.from(
			playSessionRepository
				.findByIsDeletedFalse(pageable)
				.map(this::convertToResponseDto)
		);
	}

	/**
	 * Updates an existing play session.
	 *
	 * @param id session id
	 * @param requestDto payload containing values to update
	 * @return updated play session response
	 */
	@Transactional
	public PlaySessionResponseDto update(
		UUID id,
		PlaySessionRequestDto requestDto
	) {
		PlaySession session = playSessionRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException("Play session not found")
			);

		PlaySessionResponseDto before = convertToResponseDto(session);
		session.setTableCode(requestDto.getTableCode());
		session.setSessionStatus(requestDto.getSessionStatus());

		PlaySession updatedSession = playSessionRepository.save(session);
		PlaySessionResponseDto result = convertToResponseDto(updatedSession);
		auditService.record(
			"UPDATE",
			AUDIT_ENTITY,
			session.getSessionCode(),
			before,
			result
		);
		return result;
	}

	/**
	 * Soft-deletes a play session.
	 *
	 * @param id session id
	 */
	@Transactional
	public void delete(UUID id) {
		PlaySession session = playSessionRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException("Play session not found")
			);
		PlaySessionResponseDto before = convertToResponseDto(session);
		session.setIsDeleted(true);
		playSessionRepository.save(session);
		auditService.record(
			"DELETE",
			AUDIT_ENTITY,
			session.getSessionCode(),
			before,
			null
		);
	}

	/**
	 * Checks the current status of a session by its code.
	 *
	 * @param sessionCode business session code
	 * @return current session status
	 */
	public SessionStatus checkCurrentSessionStatus(String sessionCode) {
		return findBySessionCode(sessionCode).getSessionStatus();
	}

	/**
	 * Maps a play session entity to response DTO.
	 *
	 * @param session source entity
	 * @return mapped response dto
	 */
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
