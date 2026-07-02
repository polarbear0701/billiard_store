package com.billiards.store.controller;

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
public class InvoiceController {

	private final InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<?>> create(
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

	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<?>> findAll(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String direction
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

	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
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

	@GetMapping("/invoice-code/{invoiceCode}")
	public ResponseEntity<SuccessResponse<?>> findByInvoiceCode(
		@PathVariable String invoiceCode
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

	@GetMapping("/qr")
	public ResponseEntity<SuccessResponse<?>> getQRCodeForInvoice(
		@RequestParam String invoiceCode
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

	@PutMapping("/complete")
	public ResponseEntity<SuccessResponse<?>> putMethodName(
		@RequestParam String invoiceCode
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

	@DeleteMapping("/{id}")
	public ResponseEntity<SuccessResponse<?>> delete(
		@PathVariable String invoiceCode
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
