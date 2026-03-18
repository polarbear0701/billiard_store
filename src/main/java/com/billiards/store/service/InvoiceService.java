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

    public InvoiceService(
        InvoiceRepository invoiceRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.invoiceRepository = invoiceRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    public InvoiceResponseDto create(InvoiceRequestDto requestDto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(
            codeGeneratorUtil.generateCode("INV", "invoice_code_seq")
        );
        invoice.setSessionCode(requestDto.getSessionCode());
        invoice.setTableFee(requestDto.getTableFee());
        invoice.setFnbFee(requestDto.getFnbFee());
        invoice.setTotalAmount(requestDto.getTotalAmount());
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToResponseDto(savedInvoice);
    }

    public InvoiceResponseDto findById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return convertToResponseDto(invoice);
    }

    public InvoiceResponseDto findByInvoiceCode(String invoiceCode) {
        Invoice invoice = invoiceRepository.findByInvoiceCode(invoiceCode)
            .orElseThrow(() -> new ResourceNotFoundException("No invoice associated with this invoice code. Please try again"));
        return convertToResponseDto(invoice);
    }

    public List<InvoiceResponseDto> findAll() {
        return invoiceRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDto update(UUID id, InvoiceRequestDto requestDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        invoice.setSessionCode(requestDto.getSessionCode());
        invoice.setTableFee(requestDto.getTableFee());
        invoice.setFnbFee(requestDto.getFnbFee());
        invoice.setTotalAmount(requestDto.getTotalAmount());
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return convertToResponseDto(updatedInvoice);
    }

    public void delete(UUID id) {
        invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoiceRepository.deleteById(id);
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
