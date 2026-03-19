package com.billiards.store.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.model.Invoice;
import com.billiards.store.repository.InvoiceRepository;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final PlaySessionService playSessionService;

    public InvoiceService(
        InvoiceRepository invoiceRepository,
        CodeGeneratorUtil codeGeneratorUtil,
        PlaySessionService playSessionService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
        this.playSessionService = playSessionService;
    }

    public InvoiceResponseDto create(InvoiceRequestDto requestDto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(
            codeGeneratorUtil.generateCode("INV", "invoice_code_seq")
        );

        validateReferenceCode(requestDto.getSessionCode());

        invoice.setSessionCode(requestDto.getSessionCode());
        invoice.setTableFee(requestDto.getTableFee());
        invoice.setFnbFee(requestDto.getFnbFee());
        invoice.setTotalAmount(requestDto.getTotalAmount());
        
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

    public List<InvoiceResponseDto> findAll() {
        return invoiceRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDto update(UUID id, InvoiceRequestDto requestDto) {
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        invoice.setSessionCode(requestDto.getSessionCode());
        invoice.setTableFee(requestDto.getTableFee());
        invoice.setFnbFee(requestDto.getFnbFee());
        invoice.setTotalAmount(requestDto.getTotalAmount());
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return convertToResponseDto(updatedInvoice);
    }

    public void delete(UUID id) {
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoice.setIsDeleted(true);
        invoiceRepository.save(invoice);
    }

    private void validateReferenceCode(String sessionCode) {
        if (sessionCode == null || sessionCode.trim().isEmpty()) {
            throw new ResourceNotFoundException("Session code cannot be empty");
        } 
        
        playSessionService.findBySessionCode(sessionCode);
    }

    private InvoiceResponseDto convertToResponseDto(Invoice invoice) {
        InvoiceResponseDto dto = new InvoiceResponseDto();
        dto.setInvoiceCode(invoice.getInvoiceCode());
        dto.setSessionCode(invoice.getSessionCode());
        dto.setTableFee(invoice.getTableFee());
        dto.setFnbFee(invoice.getFnbFee());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setUpdatedBy(invoice.getUpdatedBy());
        return dto;
    }
}
