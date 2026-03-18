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
    public InvoiceResponseDto create(@RequestBody InvoiceRequestDto request) {
        return invoiceService.create(request);
    }

    @GetMapping
    public List<InvoiceResponseDto> findAll() {
        return invoiceService.findAll();
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
    public InvoiceResponseDto findByInvoiceCode(@PathVariable String invoiceCode) {
        return invoiceService.findByInvoiceCode(invoiceCode);
    }

    @PutMapping("/{id}")
    public InvoiceResponseDto update(@PathVariable UUID id, @RequestBody InvoiceRequestDto request) {
        return invoiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        invoiceService.delete(id);
    }
}

