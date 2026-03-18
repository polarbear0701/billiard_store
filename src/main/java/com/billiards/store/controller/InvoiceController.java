package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.service.InvoiceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }


    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<?>> create(@RequestBody InvoiceRequestDto request) {
        InvoiceResponseDto responseDto = invoiceService.create(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<InvoiceResponseDto> invoicesDto = invoiceService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                invoicesDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        InvoiceResponseDto responseDto = invoiceService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/invoice-code/{invoiceCode}")
    public ResponseEntity<SuccessResponse<?>> findByInvoiceCode(@PathVariable String invoiceCode) {
        InvoiceResponseDto invoiceDto = invoiceService.findByInvoiceCode(invoiceCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                invoiceDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> update(@PathVariable UUID id, @RequestBody InvoiceRequestDto request) {
        InvoiceResponseDto updatedDto = invoiceService.update(id, request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                updatedDto,
                LocalDateTime.now()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Invoice deleted successfully",
                null,
                LocalDateTime.now()
        ));
    }
}

