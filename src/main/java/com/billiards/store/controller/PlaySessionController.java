package com.billiards.store.controller;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.play_session.AdjustStartTimeRequestDto;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.service.PlaySessionService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/play-session")
@Tag(
	name = "Play Session",
	description = "A customer playing on a table, from start to payment. Open to STAFF, except deleting a session (ADMIN only)."
)
public class PlaySessionController {

	private final PlaySessionService playSessionService;

	public PlaySessionController(PlaySessionService playSessionService) {
		this.playSessionService = playSessionService;
	}

	@Operation(
		operationId = "playSession_create",
		summary = "Start a play session",
		description = "Starts the clock on a table: the start time is set to now and the table becomes OCCUPIED. Fails if the table does not exist or is not available. Access: ADMIN, STAFF."
	)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<PlaySessionResponseDto>> create(
		@RequestBody PlaySessionRequestDto request
	) {
		PlaySessionResponseDto responseDto = playSessionService.create(request);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("create_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_adjustStartTime",
		summary = "Correct a session's start time",
		description = "Use when a session was started late. The new start time cannot be in the future and the session must not be completed. This changes the bill, and the change is recorded in the audit log. Access: ADMIN, STAFF."
	)
	@PutMapping("/start-time/{sessionCode}")
	public ResponseEntity<SuccessResponse<PlaySessionResponseDto>> adjustStartTime(
		@Parameter(description = "Play session code", example = "SES000001") @PathVariable String sessionCode,
		@RequestBody AdjustStartTimeRequestDto requestDto
	) {
		PlaySessionResponseDto responseDto = playSessionService.adjustStartTime(
			sessionCode,
			requestDto
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("update_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_findAll",
		summary = "List play sessions (paged)",
		description = "Returns non-deleted sessions, newest first by default. Access: ADMIN, STAFF."
	)
	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<PagedResponse<PlaySessionResponseDto>>> findAll(
		@Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
		@Parameter(description = "Field of the returned items to sort by, e.g. `createdAt` or `updatedAt`", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
		@Parameter(description = "Sort direction: `asc` or `desc`", example = "desc") @RequestParam(defaultValue = "desc") String direction
	) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);
		PagedResponse<PlaySessionResponseDto> sessionsDto =
			playSessionService.findAll(pageable);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				sessionsDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_findById",
		summary = "Get a session by internal id",
		description = "Looks a session up by its UUID. Access: ADMIN, STAFF."
	)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<PlaySessionResponseDto>> findById(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		PlaySessionResponseDto responseDto = playSessionService.findById(id);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_findBySessionCode",
		summary = "Get a session by code",
		description = "Looks a session up by its code, e.g. `SES000001`. Access: ADMIN, STAFF."
	)
	@GetMapping("/session-code/{sessionCode}")
	public ResponseEntity<SuccessResponse<PlaySessionResponseDto>> findBySessionCode(
		@Parameter(description = "Play session code", example = "SES000001") @PathVariable String sessionCode
	) {
		PlaySessionResponseDto sessionDto =
			playSessionService.findBySessionCode(sessionCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				sessionDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_getCurrentFee",
		summary = "Current table fee of a session",
		description = "`data` is the table fee so far in VND: minutes played / 60 x the table's hourly price, rounded to 2 decimals. Food and drink are not included. Works for running and finished sessions. Access: ADMIN, STAFF."
	)
	@GetMapping("/fee")
	public ResponseEntity<SuccessResponse<BigDecimal>> getCurrentFee(
		@Parameter(description = "Play session code", example = "SES000001") @RequestParam String playSessionCode
	) {
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				playSessionService.calculateTableFee(playSessionCode),
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_closePlaySession",
		summary = "End a play session",
		description = "Sets the end time to now, marks the session COMPLETED and frees the table. Fails if the session is already closed. Note: creating the invoice also ends the session, so for the normal checkout flow call `POST /invoice/create` directly and do not close the session first (the invoice request would then fail with a session-closed error). Access: ADMIN, STAFF."
	)
	@PutMapping("/close/{sessionCode}")
	public ResponseEntity<SuccessResponse<Void>> closePlaySession(
		@Parameter(description = "Play session code", example = "SES000001") @PathVariable String sessionCode
	) {
		playSessionService.endPlaySession(sessionCode);

		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("session_ended"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_getCurrentTableSession",
		summary = "Get the running session of a table",
		description = "Returns the ACTIVE session of an OCCUPIED table. Fails if the table is not in use. Access: ADMIN, STAFF."
	)
	@GetMapping("/current")
	public ResponseEntity<SuccessResponse<PlaySessionResponseDto>> getCurrentTableSession(
		@Parameter(description = "Table code", example = "TBL000001") @RequestParam("tableCode") String tableCode
	) {
		PlaySessionResponseDto playSessionResponseDto =
			playSessionService.getCurrentSessionOfOccupiedTable(tableCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				playSessionResponseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "playSession_delete",
		summary = "Delete a play session",
		description = "Soft delete by internal id. Access: ADMIN only."
	)
	@DeleteMapping("/{id}")
	public ResponseEntity<SuccessResponse<Void>> delete(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		playSessionService.delete(id);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				"success",
				null,
				LocalDateTime.now()
			)
		);
	}
}
