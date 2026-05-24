package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.util.SystemLanguage;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
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
    public ResponseEntity<SuccessResponse<?>> create(@RequestBody BilliardTableRequestDto request) {
        BilliardTableResponseDto responseDto = billiardTableService.create(request);
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.ACCEPTED.value(),
            SystemLanguage.getMessage("create_success"), 
            responseDto, 
            LocalDateTime.now()));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<BilliardTableResponseDto> tablesDto = billiardTableService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                tablesDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        BilliardTableResponseDto responseDto = billiardTableService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                responseDto,
                LocalDateTime.now()

        ));
    }

    @GetMapping("/table-code/{tableCode}")
    public ResponseEntity<SuccessResponse<?>> findByTableCode(@PathVariable String tableCode) {
        BilliardTableResponseDto tableDto = billiardTableService.findByTableCode(tableCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("success"),
                tableDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/occupy/{tableCode}/{newStatus}")
    public ResponseEntity<SuccessResponse<?>> setStatusTable(@PathVariable String tableCode, @PathVariable TableStatus newStatus) {
        BilliardTableResponseDto responseDto = billiardTableService.setStatusTable(tableCode, newStatus);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("update_success"),
                responseDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/update-price")
    public ResponseEntity<SuccessResponse<?>> updateTablePrice(@RequestParam String tableCode, @RequestParam BigDecimal newPricePerHour) {
        BilliardTableResponseDto responseDto = billiardTableService.updateTablePrice(tableCode, newPricePerHour);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("update_success"),
                responseDto,
                LocalDateTime.now()
        ));
    }

    @DeleteMapping("/{tableCode}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable String tableCode) {
        billiardTableService.delete(tableCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                SystemLanguage.getMessage("delete_success"),
                null,
                LocalDateTime.now()
        ));
    }
}
