package com.billiards.store.service;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.model.Invoice;
import com.billiards.store.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

	private final InvoiceRepository invoiceRepository;
	private final CodeGeneratorUtil codeGeneratorUtil;
	private final PlaySessionService playSessionService;
	private final OrderItemService orderItemService;
	private final String BANK_URL;

	public InvoiceService(
		InvoiceRepository invoiceRepository,
		CodeGeneratorUtil codeGeneratorUtil,
		PlaySessionService playSessionService,
		OrderItemService orderItemService,
		@Value("${app.bankUrl}") String BANK_URL
	) {
		this.invoiceRepository = invoiceRepository;
		this.codeGeneratorUtil = codeGeneratorUtil;
		this.playSessionService = playSessionService;
		this.orderItemService = orderItemService;
		this.BANK_URL = BANK_URL;
	}

	/**
	 * Creates a new invoice for the specified play session.
	 * <p>
	 * Ends the play session, validates that an invoice doesn't already exist for this session,
	 * generates a unique invoice code, calculates table fee and F&B fees, computes total amount,
	 * saves to database, and returns the response DTO. Throws ResourceNotFoundException if:
	 * - Play session not found or already ended
	 * - Invoice already exists for this session
	 * </p>
	 *
	 * @param requestDto The request data containing session code.
	 * @return An InvoiceResponseDto representing the newly created invoice.
	 */
	@Transactional
	public InvoiceResponseDto create(InvoiceRequestDto requestDto) {
		playSessionService.endPlaySession(requestDto.getSessionCode());

		if (
			invoiceRepository.existsBySessionCodeAndIsDeletedFalse(
				requestDto.getSessionCode()
			)
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("invoice_already_exists")
			);
		}

		Invoice invoice = new Invoice();
		invoice.setInvoiceCode(
			codeGeneratorUtil.generateCode("INV", "invoice_code_seq")
		);

		BigDecimal tableFee = playSessionService.calculateTableFee(
			requestDto.getSessionCode()
		);
		BigDecimal fnbFee = orderItemService.calculateTotalFnbFee(
			requestDto.getSessionCode()
		);

		invoice.setSessionCode(requestDto.getSessionCode());
		invoice.setTableFee(tableFee);
		invoice.setFnbFee(fnbFee);
		invoice.setTotalAmount(tableFee.add(fnbFee));

		Invoice savedInvoice = invoiceRepository.save(invoice);
		return convertToResponseDto(savedInvoice);
	}

	/**
	 * Finds an invoice by its unique identifier.
	 * <p>
	 * Retrieves the invoice from the database using the provided ID and throws a ResourceNotFoundException
	 * if not found or already deleted. Returns the converted response DTO with all relevant details.
	 * </p>
	 *
	 * @param id The UUID of the invoice to find.
	 * @return An InvoiceResponseDto containing the invoice data.
	 */
	public InvoiceResponseDto findById(UUID id) {
		Invoice invoice = invoiceRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("invoice_not_found")
				)
			);
		return convertToResponseDto(invoice);
	}

	/**
	 * Finds an invoice by its unique code.
	 * <p>
	 * Retrieves the invoice from the database using the provided invoice code and throws a ResourceNotFoundException
	 * if not found or already deleted. Returns the converted response DTO with all relevant details.
	 * </p>
	 *
	 * @param invoiceCode The unique code of the invoice to find.
	 * @return An InvoiceResponseDto containing the invoice data.
	 */
	public InvoiceResponseDto findByInvoiceCode(String invoiceCode) {
		Invoice invoice = invoiceRepository
			.findByInvoiceCodeAndIsDeletedFalse(invoiceCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("invoice_not_found")
				)
			);
		return convertToResponseDto(invoice);
	}

	/**
	 * Retrieves the total amount for a specific invoice.
	 * <p>
	 * Fetches the total amount from the database using the invoice code and throws a ResourceNotFoundException
	 * if not found or already deleted. Returns the total amount as a BigDecimal.
	 * </p>
	 *
	 * @param invoiceCode The unique code of the invoice to retrieve.
	 * @return A BigDecimal representing the total amount of the invoice.
	 */
	public BigDecimal getTotalAmount(String invoiceCode) {
		return invoiceRepository
			.findTotalAmountByInvoiceCode(invoiceCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("invoice_not_found")
				)
			);
	}

	/**
	 * Generates a bank transaction QR code URL for the specified invoice.
	 * <p>
	 * Retrieves the total amount from the invoice, formats it into a QR payment URL with addInfo and accountName,
	 * and returns the formatted URL string. The generated URL includes Vietnamese text for "Chuyen khoan tien Bida 298"
	 * as the transaction info and "Phan Trong Nguyen" as the account name.
	 * </p>
	 *
	 * @param invoiceCode The unique code of the invoice to generate payment for.
	 * @return A String containing the bank transaction QR URL with formatted parameters.
	 */
	public String generateBankTransaction(String invoiceCode) {
		BigDecimal totalAmount = getTotalAmount(invoiceCode);
		// amount=100000&addInfo=Test&accountName=Phan%20Trong%20Nguyen
		String qrURL =
			BANK_URL +
			String.format("amount=%.0f&", totalAmount.doubleValue()) +
			String.format("addInfo=%s&", "Chuyen khoan tien Bida 298") +
			String.format("accountName=%s", "Phan%20Trong%20Nguyen");
		return qrURL;
	}

	/**
	 * Retrieves all active invoices (not deleted).
	 * <p>
	 * Fetches all non-deleted invoices from the database, converts them to response DTOs, and returns as a list.
	 * </p>
	 *
	 * @return A List of InvoiceResponseDto containing all active invoice data.
	 */
	public List<InvoiceResponseDto> findAll() {
		return invoiceRepository
			.findByIsDeletedFalse()
			.stream()
			.map(this::convertToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieves a page of active invoices (not deleted).
	 *
	 * @param pageable the pagination and sorting information
	 * @return a paginated response of active invoices as response DTOs
	 */
	public PagedResponse<InvoiceResponseDto> findAll(Pageable pageable) {
		return PagedResponse.from(
			invoiceRepository
				.findByIsDeletedFalse(pageable)
				.map(this::convertToResponseDto)
		);
	}

	/**
	 * Marks an invoice as complete.
	 * <p>
	 * Retrieves the invoice by its code, sets the complete flag to true, and saves it to the database.
	 * Throws ResourceNotFoundException if not found or already deleted.
	 * </p>
	 *
	 * @param invoiceCode The unique code of the invoice to mark as complete.
	 */
	public void completeInvoice(String invoiceCode) {
		Invoice invoice = invoiceRepository
			.findByInvoiceCodeAndIsDeletedFalse(invoiceCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("invoice_not_found")
				)
			);
		invoice.setComplete(true);
		invoiceRepository.save(invoice);
	}

	/**
	 * Deletes an invoice by its unique code.
	 * <p>
	 * Retrieves the invoice by its code, marks it as deleted, and saves to the database.
	 * Throws ResourceNotFoundException if not found or already deleted.
	 * </p>
	 *
	 * @param invoiceCode The unique code of the invoice to delete.
	 */
	public void delete(String invoiceCode) {
		Invoice invoice = invoiceRepository
			.findByInvoiceCodeAndIsDeletedFalse(invoiceCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("invoice_not_found")
				)
			);
		invoice.setIsDeleted(true);
		invoiceRepository.save(invoice);
	}

	/**
	 * Converts an Invoice entity to its corresponding response DTO.
	 * <p>
	 * Maps all fields from the Invoice entity to the InvoiceResponseDto, including timestamps and user information.
	 * </p>
	 *
	 * @param invoice The invoice entity to convert.
	 * @return The converted InvoiceResponseDto.
	 */
	private InvoiceResponseDto convertToResponseDto(Invoice invoice) {
		InvoiceResponseDto dto = new InvoiceResponseDto();
		dto.setInvoiceCode(invoice.getInvoiceCode());
		dto.setSessionCode(invoice.getSessionCode());
		dto.setTableFee(invoice.getTableFee());
		dto.setFnbFee(invoice.getFnbFee());
		dto.setTotalAmount(invoice.getTotalAmount());
		dto.setComplete(invoice.isComplete());
		dto.setCreatedAt(invoice.getCreatedAt());
		dto.setUpdatedAt(invoice.getUpdatedAt());
		dto.setCreatedBy(invoice.getCreatedBy());
		dto.setUpdatedBy(invoice.getUpdatedBy());
		return dto;
	}
}
