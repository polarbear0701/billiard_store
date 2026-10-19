package com.billiards.store.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.service.BilliardTableService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

/**
 * REST controller for billiard table operations.
 * Provides HTTP endpoints for managing billiard tables in the store, including
 * creating, reading, updating, and deleting table records.
 * <p>
 * This controller uses Spring Web MVC annotations to expose REST APIs that map
 * to CRUD operations on billiard tables, following standard HTTP conventions.
 */
@RestController
@RequestMapping("/table")
@Tag(
	name = "Billiard Table",
	description = "Physical tables and their status. Reading and changing status is open to STAFF; creating, repricing and deleting tables is ADMIN only."
)
public class BilliardTableController {

	private final BilliardTableService billiardTableService;

	/**
	 * Constructor for the BilliardTableController.
	 *
	 * <p>Depends on the BilliardTableService to handle business logic operations.</p>
	 *
	 * @param billiardTableService The service responsible for table data operations.
	 */
	public BilliardTableController(BilliardTableService billiardTableService) {
		this.billiardTableService = billiardTableService;
	}

	/**
	 * Creates a new billiard table in the store.
	 *
	 * <p>When invoked, this endpoint accepts a request containing the details for a
	 * new billiard table and returns an acknowledged success response. The status is set
	 * to ACCEPTED per HTTP semantics for resource creation operations.</p>
	 *
	 * @param request {@link BilliardTableRequestDto} object containing table information.
	 *                Includes fields such as table code, location, price, and initial status.
	 * @return A {@code ResponseEntity} with a 202 Accepted status code and a response
	 *         indicating successful table creation. The body contains the created table
	 *         details along with a timestamp.
	 */
	@Operation(
		operationId = "billiardTable_create",
		summary = "Create a table",
		description = "Creates a table with a generated code such as `TBL000001`. `pricePerHour` must be greater than 0. Access: ADMIN only."
	)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<BilliardTableResponseDto>> create(
		@RequestBody BilliardTableRequestDto request
	) {
		BilliardTableResponseDto responseDto = billiardTableService.create(
			request
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("create_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Retrieves all billiard tables from the store.
	 *
	 * <p>This endpoint provides a paginated or filtered list of all active tables,
	 * useful for inventory management, display purposes, or system administration.</p>
	 *
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and a list of table
	 *         details. Each entry contains the table's unique identifier, code, current
	 *         status, and pricing information.
	 */
	@Operation(
		operationId = "billiardTable_findAll",
		summary = "List all tables",
		description = "Returns every non-deleted table (not paged), including its current status. Access: ADMIN, STAFF."
	)
	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<List<BilliardTableResponseDto>>> findAll() {
		List<BilliardTableResponseDto> tablesDto =
			billiardTableService.findAll();
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				tablesDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Retrieves a single billiard table by its unique identifier.
	 *
	 * <p>Uses the provided UUID to locate and return the corresponding table record
	 * from the database. This endpoint is ideal for retrieving specific table details
	 * when the user already knows the table's internal ID.</p>
	 *
	 * @param id The unique identifier of the target billiard table.
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and the table details,
	 *         or an error response if the table is not found (404).
	 */
	@Operation(
		operationId = "billiardTable_findById",
		summary = "Get a table by internal id",
		description = "Looks a table up by its UUID. Access: ADMIN, STAFF."
	)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<BilliardTableResponseDto>> findById(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		BilliardTableResponseDto responseDto = billiardTableService.findById(
			id
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Retrieves a billiard table by its unique code.
	 *
	 * <p>Looks up the table record using the publicly accessible table code, which is
	 * often a human-readable identifier such as "TABLE-A1". This method provides an
	 * alternative way to find tables based on their external-facing identifier rather than
	 * internal UUIDs.</p>
	 *
	 * @param tableCode The unique code identifying the billiard table.
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and the table details,
	 *         or an error response if no table exists with the provided code (404).
	 */
	@Operation(
		operationId = "billiardTable_findByTableCode",
		summary = "Get a table by code",
		description = "Looks a table up by its code, e.g. `TBL000001`. Access: ADMIN, STAFF."
	)
	@GetMapping("/table-code/{tableCode}")
	public ResponseEntity<SuccessResponse<BilliardTableResponseDto>> findByTableCode(
		@Parameter(description = "Table code", example = "TBL000001") @PathVariable String tableCode
	) {
		BilliardTableResponseDto tableDto =
			billiardTableService.findByTableCode(tableCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				tableDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Updates the current status of a specific billiard table.
	 *
	 * <p>Modifies the occupancy or availability state of a table by setting it to a new
	 * {@link TableStatus}. This is commonly used in scheduling systems to reserve or release
	 * tables for upcoming bookings.</p>
	 *
	 * @param tableCode The unique code of the billiard table to update.
	 * @param newStatus The desired status to set (e.g., AVAILABLE, OCCUPIED, MAINTENANCE).
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and a response indicating
	 *         the successful update operation. Returns an updated table representation
	 *         with its new status value.
	 */
	@Operation(
		operationId = "billiardTable_setStatusTable",
		summary = "Change a table's status",
		description = "Sets AVAILABLE, OCCUPIED or MAINTENANCE. Rejected when the table already has that status, when going from MAINTENANCE straight to OCCUPIED, or from OCCUPIED to MAINTENANCE. Starting and ending a play session changes the status automatically, so the UI normally only needs this for maintenance. Access: ADMIN, STAFF."
	)
	@PutMapping("/occupy/{tableCode}/{newStatus}")
	public ResponseEntity<SuccessResponse<BilliardTableResponseDto>> setStatusTable(
		@Parameter(description = "Table code", example = "TBL000001") @PathVariable String tableCode,
		@Parameter(description = "The status to set") @PathVariable TableStatus newStatus
	) {
		BilliardTableResponseDto responseDto =
			billiardTableService.setStatusTable(tableCode, newStatus);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("update_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Updates the hourly price for a billiard table.
	 *
	 * <p>Changes the rate associated with a specific table, allowing operators to adjust
	 * pricing dynamically based on demand, promotions, or seasonal variations. This method
	 * applies price updates only to the single specified table.</p>
	 *
	 * @param tableCode The unique code of the billiard table whose price is being modified.
	 * @param newPricePerHour The new hourly rate for the table in the local currency.
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and a response indicating
	 *         the successful price update. Returns the updated table object with its new
	 *         price value.
	 */
	@Operation(
		operationId = "billiardTable_updateTablePrice",
		summary = "Change a table's hourly price",
		description = "Rejected while the table is OCCUPIED, or when the price is not greater than 0. Access: ADMIN only."
	)
	@PutMapping("/update-price")
	public ResponseEntity<SuccessResponse<BilliardTableResponseDto>> updateTablePrice(
		@Parameter(description = "Table code", example = "TBL000001") @RequestParam String tableCode,
		@Parameter(description = "New hourly rate in VND, must be greater than 0", example = "60000") @RequestParam BigDecimal newPricePerHour
	) {
		BilliardTableResponseDto responseDto =
			billiardTableService.updateTablePrice(tableCode, newPricePerHour);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("update_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	/**
	 * Deletes a billiard table from the store database.
	 *
	 * <p>Removes the specified table by its unique code. The operation is final and cannot
	 * be undone. Ensure that all reservations referencing the table have been handled before
	 * removing it to avoid data inconsistencies.</p>
	 *
	 * @param tableCode The unique code of the billiard table to delete.
	 * @return A {@code ResponseEntity} with HTTP 200 OK status and an empty response body,
	 *         indicating that the deletion was processed successfully. Returns a 404 Not
	 *         Found if the specified table does not exist in the database.
	 */
	@Operation(
		operationId = "billiardTable_delete",
		summary = "Delete a table",
		description = "Soft delete. Access: ADMIN only."
	)
	@DeleteMapping("/{tableCode}")
	public ResponseEntity<SuccessResponse<Void>> delete(
		@Parameter(description = "Table code", example = "TBL000001") @PathVariable String tableCode
	) {
		billiardTableService.delete(tableCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("delete_success"),
				null,
				LocalDateTime.now()
			)
		);
	}
}
