package com.billiards.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.model.Invoice;
import com.billiards.store.repository.InvoiceRepository;

import jakarta.transaction.Transactional;

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

    @Transactional
    public InvoiceResponseDto create(InvoiceRequestDto requestDto) {

        playSessionService.endPlaySession(requestDto.getSessionCode());

        if (invoiceRepository.existsBySessionCodeAndIsDeletedFalse(requestDto.getSessionCode())) {
            throw new ResourceNotFoundException("This session already has invoice generated");
        }
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(
            codeGeneratorUtil.generateCode("INV", "invoice_code_seq")
        );

        BigDecimal tableFee = playSessionService.calculateTableFee(requestDto.getSessionCode());
        BigDecimal fnbFee = orderItemService.calculateTotalFnbFee(requestDto.getSessionCode());

        invoice.setSessionCode(requestDto.getSessionCode());
        invoice.setTableFee(tableFee);
        invoice.setFnbFee(fnbFee);
        invoice.setTotalAmount(tableFee.add(fnbFee));
        
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToResponseDto(savedInvoice);
    }

    public InvoiceResponseDto findById(UUID id) {
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return convertToResponseDto(invoice);
    }

    public InvoiceResponseDto findByInvoiceCode(String invoiceCode) {
        Invoice invoice = invoiceRepository.findByInvoiceCodeAndIsDeletedFalse(invoiceCode)
            .orElseThrow(() -> new ResourceNotFoundException("No invoice associated with this invoice code. Please try again"));
        return convertToResponseDto(invoice);
    }

    public BigDecimal getTotalAmount(String invoiceCode) {
        return invoiceRepository.findTotalAmountByInvoiceCode(invoiceCode)
            .orElseThrow(() -> new ResourceNotFoundException("Cannot find this invoice"));
    }

    public String generateBankTransaction(String invoiceCode) {
        BigDecimal totalAmount = getTotalAmount(invoiceCode);
        // amount=100000&addInfo=Test&accountName=Phan%20Trong%20Nguyen
        String qrURL = BANK_URL 
            + String.format("amount=%.0f&", totalAmount.doubleValue())
            + String.format("addInfo=%s&", "Chuyen khoan tien Bida 298")
            + String.format("accountName=%s", "Phan%20Trong%20Nguyen");
        return qrURL;

    }

    public List<InvoiceResponseDto> findAll() {
        return invoiceRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public void completeInvoice(String invoiceCode) {
        Invoice invoice = invoiceRepository.findByInvoiceCodeAndIsDeletedFalse(invoiceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoice.setComplete(true);
        invoiceRepository.save(invoice);
    }

    public void delete(UUID id) {
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoice.setIsDeleted(true);
        invoiceRepository.save(invoice);
    }

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
