package com.billiards.store.service;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.model.BilliardTable;
import com.billiards.store.repository.BilliardTableRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service for managing billiard table operations.
 * <p>
 * This service provides CRUD operations and business logic handling for billiard tables,
 * including creation, retrieval, updates, deletion, and status management.
 */
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

    /**
     * Creates a new billiard table with the provided details.
     * <p>
     * Generates a unique table code, validates that the price is positive,
     * and saves the new billiard table to the repository.
     *
     * @param requestDto the request DTO containing table details
     * @return the created billiard table as a response DTO
     * @throws ResourceNotFoundException if the price per hour is not positive
     */
    public BilliardTableResponseDto create(BilliardTableRequestDto requestDto) {
        BilliardTable table = new BilliardTable();
        table.setTableCode(
            codeGeneratorUtil.generateCode("TBL", "billiard_table_code_seq")
        );
        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());

        if (
            requestDto.getPricePerHour().compareTo(BigDecimal.valueOf(0)) <= 0
        ) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("price_must_be_positive")
            );
        }
        table.setPricePerHour(requestDto.getPricePerHour());

        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    /**
     * Retrieves a billiard table by its unique identifier.
     *
     * @param id the unique identifier of the billiard table
     * @return the billiard table as a response DTO
     * @throws ResourceNotFoundException if no billiard table is found with the given ID
     */
    public BilliardTableResponseDto findById(UUID id) {
        BilliardTable table = billiardTableRepository
            .findByIdAndIsDeletedFalse(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );
        return convertToResponseDto(table);
    }

    /**
     * Retrieves a billiard table by its table code.
     *
     * @param tableCode the unique table code
     * @return the billiard table as a response DTO
     * @throws ResourceNotFoundException if no billiard table is found with the given table code
     */
    public BilliardTableResponseDto findByTableCode(String tableCode) {
        BilliardTable table = billiardTableRepository
            .findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );
        return convertToResponseDto(table);
    }

    /**
     * Retrieves the price per hour for a billiard table by its table code.
     *
     * @param tableCode the unique table code
     * @return the price per hour as a BigDecimal
     * @throws ResourceNotFoundException if no billiard table is found with the given table code
     */
    public BigDecimal getTablePricePerHour(String tableCode) {
        BilliardTable table = billiardTableRepository
            .findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );
        return table.getPricePerHour();
    }

    /**
     * Retrieves all active billiard tables (non-deleted).
     *
     * @return a list of billiard tables as response DTOs
     */
    public List<BilliardTableResponseDto> findAll() {
        return billiardTableRepository
            .findByIsDeletedFalse()
            .stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the current status of a billiard table by its table code.
     *
     * @param tableCode the unique table code
     * @return the current table status
     * @throws ResourceNotFoundException if no billiard table is found with the given table code
     */
    public TableStatus getTableStatus(String tableCode) {
        return billiardTableRepository
            .findStatusByTableCode(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException("Table not found")
            );
    }

    /**
     * Updates the details of an existing billiard table.
     * <p>
     * Updates the table type, status, and price per hour. Throws an exception if
     * the table is not found or does not exist (soft-deleted).
     *
     * @param id the unique identifier of the billiard table to update
     * @param requestDto the request DTO containing updated details
     * @return the updated billiard table as a response DTO
     * @throws ResourceNotFoundException if no billiard table is found with the given ID
     */
    public BilliardTableResponseDto update(
        UUID id,
        BilliardTableRequestDto requestDto
    ) {
        BilliardTable table = billiardTableRepository
            .findByIdAndIsDeletedFalse(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );

        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());
        table.setPricePerHour(requestDto.getPricePerHour());

        BilliardTable updatedTable = billiardTableRepository.save(table);
        return convertToResponseDto(updatedTable);
    }

    /**
     * Updates the price per hour for a billiard table.
     * <p>
     * Validates that the table is not currently occupied before allowing price updates.
     * Ensures the new price is positive and handles database update errors.
     *
     * @param tableCode the unique table code
     * @param newPricePerHour the new price per hour
     * @return the updated billiard table as a response DTO
     * @throws ResourceNotFoundException if the table is not found, occupied, or the price is invalid
     */
    @Transactional
    public BilliardTableResponseDto updateTablePrice(
        String tableCode,
        BigDecimal newPricePerHour
    ) {
        BilliardTable table = billiardTableRepository
            .findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );
        table.setPricePerHour(newPricePerHour);
        if (table.getTableStatus() == TableStatus.OCCUPIED) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("table_in_use")
            );
        }

        if (newPricePerHour.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("price_must_be_positive")
            );
        }

        try {
            BilliardTable updatedTable = billiardTableRepository.save(table);
            return convertToResponseDto(updatedTable);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to update table price");
        }
    }

    /**
     * Soft-deletes a billiard table by its table code.
     * <p>
     * This method marks the table as deleted without removing it from the database,
     * allowing for potential recovery and maintaining referential integrity.
     *
     * @param tableCode the unique table code of the billiard table to delete
     */
    public void delete(String tableCode) {
        setTableToDelete(tableCode);
    }

    /**
     * Updates the status of a billiard table by its table code.
     * <p>
     * Validates that the new status is different from the current status and
     * handles transitions between Maintenance and Occupied states to prevent
     * invalid status changes.
     *
     * @param tableCode the unique table code
     * @param newStatus the new status to set for the billiard table
     * @return the updated billiard table as a response DTO
     * @throws ResourceNotFoundException if the table is not found, status cannot be changed,
     *         or the table is in an incompatible state for the transition
     */
    public BilliardTableResponseDto setStatusTable(
        String tableCode,
        TableStatus newStatus
    ) {
        BilliardTable table = billiardTableRepository
            .findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    SystemLanguage.getMessage("table_not_found")
                )
            );

        if (table.getTableStatus() == newStatus) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("table_not_found")
            );
        }

        if (
            table.getTableStatus() == TableStatus.MAINTENANCE &&
            newStatus == TableStatus.OCCUPIED
        ) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("table_not_found")
            );
        }

        if (
            table.getTableStatus() == TableStatus.OCCUPIED &&
            newStatus == TableStatus.MAINTENANCE
        ) {
            throw new ResourceNotFoundException(
                SystemLanguage.getMessage("table_in_use")
            );
        }

        table.setTableStatus(newStatus);
        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    /**
     * Converts a {@link BilliardTable} entity to a {@link BilliardTableResponseDto}.
     *
     * @param table the billiard table entity to convert
     * @return the response DTO containing all table details
     */
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

    /**
     * Soft-deletes a billiard table by marking it as deleted.
     *
     * @param tableCode the unique table code of the billiard table to delete
     */
    private void setTableToDelete(String tableCode) {
        BilliardTable targetTable = billiardTableRepository
            .findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() ->
                new ResourceNotFoundException("Table not found")
            );
        targetTable.setIsDeleted(true);
        billiardTableRepository.save(targetTable);
    }
}
