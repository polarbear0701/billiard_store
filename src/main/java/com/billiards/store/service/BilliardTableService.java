package com.billiards.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.model.BilliardTable;
import com.billiards.store.repository.BilliardTableRepository;
import com.billiards.store.common.enumerated.TableStatus;

@Service
public class BilliardTableService {
    private final BilliardTableRepository billiardTableRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;

    public BilliardTableService(
        BilliardTableRepository billiardTableRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.billiardTableRepository = billiardTableRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    public BilliardTableResponseDto create(BilliardTableRequestDto requestDto) {
        BilliardTable table = new BilliardTable();
        table.setTableCode(
            codeGeneratorUtil.generateCode("TBL", "billiard_table_code_seq")
        );
        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());
        
        if (requestDto.getPricePerHour().compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new ResourceNotFoundException("Cannot have price smaller than or equal to 0");
        }
        table.setPricePerHour(requestDto.getPricePerHour());
        
        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    public BilliardTableResponseDto findById(UUID id) {
        BilliardTable table = billiardTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        return convertToResponseDto(table);
    }

    public BilliardTableResponseDto findByTableCode(String tableCode) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("No table associated with this table code. Please try again"));
        return convertToResponseDto(table);
    }

    public BigDecimal getTablePricePerHour(String tableCode) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        return table.getPricePerHour();
    }

    public List<BilliardTableResponseDto> findAll() {
        return billiardTableRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public TableStatus getTableStatus(String tableCode) {
        return billiardTableRepository.findStatusByTableCode(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    public BilliardTableResponseDto update(UUID id, BilliardTableRequestDto requestDto) {
        BilliardTable table = billiardTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        
        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());
        table.setPricePerHour(requestDto.getPricePerHour());
        
        BilliardTable updatedTable = billiardTableRepository.save(table);
        return convertToResponseDto(updatedTable);
    }

    public void delete(String tableCode) {
        setTableToDelete(tableCode);
    }

    public BilliardTableResponseDto setStatusTable(String tableCode, TableStatus newStatus) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        
        if (table.getTableStatus() == newStatus) {
            throw new ResourceNotFoundException("Table is already in " + newStatus);
        }

        if (table.getTableStatus() == TableStatus.MAINTENANCE && newStatus == TableStatus.OCCUPIED) {
            throw new ResourceNotFoundException("Table is currently in maintainance.");
        }

        if (table.getTableStatus() == TableStatus.OCCUPIED && newStatus == TableStatus.MAINTENANCE) {
            throw new ResourceNotFoundException("Table is currently in use.");
        }


        table.setTableStatus(newStatus);
        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    private BilliardTableResponseDto convertToResponseDto(BilliardTable table) {
        BilliardTableResponseDto dto = new BilliardTableResponseDto();
        dto.setTableCode(table.getTableCode());
        dto.setTableType(table.getTableType());
        dto.setTableStatus(table.getTableStatus());
        dto.setPricePerHour(table.getPricePerHour());
        dto.setCreatedAt(table.getCreatedAt());
        dto.setUpdatedAt(table.getUpdatedAt());
        dto.setCreatedBy(table.getCreatedBy());
        dto.setUpdatedBy(table.getUpdatedBy());
        return dto;
    }

    private void setTableToDelete(String tableCode) {
        BilliardTable targetTable = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        targetTable.setIsDeleted(true);
        billiardTableRepository.save(targetTable);
    }
}
