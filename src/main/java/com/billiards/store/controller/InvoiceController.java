package com.billiards.store.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.service.InvoiceService;
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
@RequestMapping("/invoice")
@Tag(
	name = "Invoice",
	description = "Bills for finished play sessions. Open to STAFF, except deleting an invoice (ADMIN only)."
)
public class InvoiceController {

	private final InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Operation(
		operationId = "invoice_create",
		summary = "Create the invoice for a session (checkout)",
		description = "Ends the play session, then calculates the table fee and the food and drink total. Only one invoice can exist per session, and the session must still be ACTIVE (do not call `/play-session/close` first). The invoice starts unpaid (`complete=false`). Access: ADMIN, STAFF."
	)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<InvoiceResponseDto>> create(
		@RequestBody InvoiceRequestDto request
	) {
		InvoiceResponseDto responseDto = invoiceService.create(request);
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
		operationId = "invoice_findAll",
		summary = "List invoices (paged)",
		description = "Returns non-deleted invoices, newest first by default. Access: ADMIN, STAFF."
	)
	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<PagedResponse<InvoiceResponseDto>>> findAll(
		@Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
		@Parameter(description = "Field of the returned items to sort by, e.g. `createdAt` or `updatedAt`", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
		@Parameter(description = "Sort direction: `asc` or `desc`", example = "desc") @RequestParam(defaultValue = "desc") String direction
	) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);
		PagedResponse<InvoiceResponseDto> invoicesDto = invoiceService.findAll(
			pageable
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				invoicesDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "invoice_findById",
		summary = "Get an invoice by internal id",
		description = "Looks an invoice up by its UUID. Access: ADMIN, STAFF."
	)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<InvoiceResponseDto>> findById(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		InvoiceResponseDto responseDto = invoiceService.findById(id);
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
		operationId = "invoice_findByInvoiceCode",
		summary = "Get an invoice by code",
		description = "Looks an invoice up by its code, e.g. `INV000001`. Access: ADMIN, STAFF."
	)
	@GetMapping("/invoice-code/{invoiceCode}")
	public ResponseEntity<SuccessResponse<InvoiceResponseDto>> findByInvoiceCode(
		@Parameter(description = "Invoice code", example = "INV000001") @PathVariable String invoiceCode
	) {
		InvoiceResponseDto invoiceDto = invoiceService.findByInvoiceCode(
			invoiceCode
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				invoiceDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "invoice_getQRCodeForInvoice",
		summary = "Get the payment QR link",
		description = "`data` is a bank-transfer QR image URL for the invoice's total amount. Access: ADMIN, STAFF."
	)
	@GetMapping("/qr")
	public ResponseEntity<SuccessResponse<String>> getQRCodeForInvoice(
		@Parameter(description = "Invoice code", example = "INV000001") @RequestParam String invoiceCode
	) {
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				invoiceService.generateBankTransaction(invoiceCode),
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "invoice_completeInvoice",
		summary = "Mark an invoice as paid",
		description = "Sets `complete=true` and records `paidAt` the first time. Calling it again changes nothing. Revenue in the dashboard is counted from this moment. Access: ADMIN, STAFF."
	)
	@PutMapping("/complete")
	public ResponseEntity<SuccessResponse<Void>> completeInvoice(
		@Parameter(description = "Invoice code", example = "INV000001") @RequestParam String invoiceCode
	) {
		invoiceService.completeInvoice(invoiceCode);

		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("invoice_paid"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "invoice_delete",
		summary = "Delete an invoice",
		description = "Soft delete. Deleted invoices no longer count as revenue. Access: ADMIN only."
	)
	@DeleteMapping("/{invoiceCode}")
	public ResponseEntity<SuccessResponse<Void>> delete(
		@Parameter(description = "Invoice code", example = "INV000001") @PathVariable String invoiceCode
	) {
		invoiceService.delete(invoiceCode);
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
