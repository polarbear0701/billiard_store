package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.service.BilliardTableService;

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
@RequestMapping("/table")
public class BilliardTableController {
    private final BilliardTableService billiardTableService;

    public BilliardTableController (BilliardTableService billiardTableService) {
        this.billiardTableService = billiardTableService;
    }

    
    @PostMapping("/create")
    public BilliardTableResponseDto create(@RequestBody BilliardTableRequestDto request) {
        return billiardTableService.create(request);
    }

    @GetMapping
    public List<BilliardTableResponseDto> findAll() {
        return billiardTableService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        BilliardTableResponseDto responseDto = billiardTableService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                responseDto,
                LocalDateTime.now()

        ));
    }

    @GetMapping("/table-code/{tableCode}")
    public BilliardTableResponseDto findByTableCode(@PathVariable String tableCode) {
        return billiardTableService.findByTableCode(tableCode);
    }

    @PutMapping("/{id}")
    public BilliardTableResponseDto update(@PathVariable UUID id, @RequestBody BilliardTableRequestDto request) {
        return billiardTableService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        billiardTableService.delete(id);
    }
}
