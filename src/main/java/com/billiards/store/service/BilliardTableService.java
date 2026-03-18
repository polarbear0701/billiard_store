package com.billiards.store.service;

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
        table.setPricePerHour(requestDto.getPricePerHour());
        
        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    public BilliardTableResponseDto findById(UUID id) {
        BilliardTable table = billiardTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        return convertToResponseDto(table);
    }

    public BilliardTableResponseDto findByTableCode(String tableCode) {
        BilliardTable table = billiardTableRepository.findByTableCode(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("No table associated with this table code. Please try again"));
        return convertToResponseDto(table);
    }

    public List<BilliardTableResponseDto> findAll() {
        return billiardTableRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public BilliardTableResponseDto update(UUID id, BilliardTableRequestDto requestDto) {
        BilliardTable table = billiardTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        
        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());
        table.setPricePerHour(requestDto.getPricePerHour());
        
        BilliardTable updatedTable = billiardTableRepository.save(table);
        return convertToResponseDto(updatedTable);
    }

    public void delete(UUID id) {
        billiardTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billiard table not found"));
        billiardTableRepository.deleteById(id);
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
}
