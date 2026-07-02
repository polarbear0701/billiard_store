package com.billiards.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.model.BilliardTable;
import com.billiards.store.repository.BilliardTableRepository;

import jakarta.transaction.Transactional;

import com.billiards.store.common.enumerated.TableStatus;

/**
 * Service layer for billiard table data operations.
 * Handles business logic for CRUD operations on billiard tables in the store,
 * including validation, transaction management, and resource lifecycle handling.
 *
 * <p><p>This service acts as a bridge between the REST controller and the database
 * repository layer, implementing business rules such as price validation, occupancy
 * checks, and data transformation between model and DTO formats.</p>*/
@Service
public class BilliardTableService {
    private final BilliardTableRepository billiardTableRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;

    /**
     * Constructor for the BilliardTableService.
     *
     * <p>Depends on {@link BilliardTableRepository} for data access and
     * {@link CodeGeneratorUtil} for generating unique table codes.</p>
     *
     * @param billiardTableRepository The repository layer for database operations.
     * @param codeGeneratorUtil Utility for generating unique table codes.
     */
    public BilliardTableService(
        BilliardTableRepository billiardTableRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.billiardTableRepository = billiardTableRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    /**
     * Creates a new billiard table record in the database.
     *
     * <p>When invoked, this method initializes a new BilliardTable entity and assigns
     * an auto-generated unique table code. It validates that the price per hour is positive,
     * throwing an exception if the provided price is invalid. The table status defaults to
     * {@link TableStatus#AVAILABLE} based on the request.</p>
     *
     * @param requestDto {@link BilliardTableRequestDto} object containing initial table information.
     *                   Includes fields such as table type, current status, and hourly price.
     * @return A {@code BilliardTableResponseDto} containing the details of the newly created
     *         table, including its auto-generated code, timestamps, and metadata.
     * @throws ResourceNotFoundException if the provided price is not positive or other validation fails.
     */
    public BilliardTableResponseDto create(BilliardTableRequestDto requestDto) {
        BilliardTable table = new BilliardTable();
        table.setTableCode(
            codeGeneratorUtil.generateCode("TBL", "billiard_table_code_seq")
        );
        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());

        if (requestDto.getPricePerHour().compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("price_must_be_positive"));
        }
        table.setPricePerHour(requestDto.getPricePerHour());

        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    /**
     * Retrieves a single billiard table by its unique identifier.
     *
     * <p>This method fetches a table record from the database using the provided UUID and
     * ensures that soft-deleted records are excluded. It transforms the entity into a DTO
     * for response formatting.</p>
     *
     * @param id The unique identifier (UUID) of the target billiard table.
     * @return A {@code BilliardTableResponseDto} with full table details including type,
     *         status, and pricing information.
     * @throws ResourceNotFoundException if no non-deleted table exists with the provided ID.
     */
    public BilliardTableResponseDto findById(UUID id) {
        BilliardTable table = billiardTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));
        return convertToResponseDto(table);
    }

    /**
     * Retrieves a billiard table by its unique code.
     *
     * <p>Looks up the table record using the publicly accessible table code (e.g., "TABLE-A1"),
     * ensuring that soft-deleted records are excluded. Returns the entity data formatted
     * as a response DTO.</p>
     *
     * @param tableCode The unique code identifying the billiard table.
     * @return A {@code BilliardTableResponseDto} containing the requested table's information.
     * @throws ResourceNotFoundException if no table exists with the provided code or it has been soft-deleted.
     */
    public BilliardTableResponseDto findByTableCode(String tableCode) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));
        return convertToResponseDto(table);
    }

    /**
     * Retrieves the hourly price for a specific billiard table.
     *
     * <p>Fetches the price per hour value from the database using the table code and
     * ensures that soft-deleted tables are not returned. This method is commonly used
     * in pricing display APIs or schedule displays.</p>
     *
     * @param tableCode The unique code of the billiard table.
     * @return The price per hour as a {@code BigDecimal} for the specified table.
     * @throws ResourceNotFoundException if no non-deleted table exists with the provided code.
     */
    public BigDecimal getTablePricePerHour(String tableCode) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
                .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));
        return table.getPricePerHour();
    }

    /**
     * Retrieves all active billiard tables from the store.
     *
     * <p>This method retrieves records excluding soft-deleted items and maps each to a
     * {@link BilliardTableResponseDto} for response formatting. The returned list contains
     * full table details including type, status, price, and metadata.</p>
     *
     * @return A {@code List} of {@link BilliardTableResponseDto} objects representing all
     *         active tables in the system.
     */
    public List<BilliardTableResponseDto> findAll() {
        return billiardTableRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the current status of a specific billiard table.
     *
     * <p>Fetches the {@link TableStatus} value from the database using the table code,
     * ensuring that soft-deleted tables are not included. This method is typically used
     * by controllers to determine table availability or occupancy state for display purposes.</p>
     *
     * @param tableCode The unique code of the billiard table.
     * @return The current {@link TableStatus} of the specified table.
     * @throws ResourceNotFoundException if no non-deleted table exists with the provided code.
     */
    public TableStatus getTableStatus(String tableCode) {
        return billiardTableRepository.findStatusByTableCode(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    /**
     * Updates a billiard table's fields by its unique identifier.
     *
     * <p>This method updates the entity's table type, status, and price per hour for the
     * provided ID. It overwrites all changes and saves the modified record, returning the
     * updated entity as a DTO. Similar to PUT semantics.</p>
     *
     * @param id The unique identifier (UUID) of the target billiard table.
     * @param requestDto {@link BilliardTableRequestDto} containing update data for type,
     *                   status, and price per hour.
     * @return A {@code BilliardTableResponseDto} reflecting the updated table state.
     * @throws ResourceNotFoundException if no non-deleted table exists with the provided ID.
     */
    public BilliardTableResponseDto update(UUID id, BilliardTableRequestDto requestDto) {
        BilliardTable table = billiardTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));

        table.setTableType(requestDto.getTableType());
        table.setTableStatus(requestDto.getTableStatus());
        table.setPricePerHour(requestDto.getPricePerHour());

        BilliardTable updatedTable = billiardTableRepository.save(table);
        return convertToResponseDto(updatedTable);
    }

    /**
     * Updates the price per hour for a specific billiard table in a transactional manner.
     *
     * <p>This method modifies only the pricing field of a table using its code, ensuring
     * that soft-deleted tables are excluded. It performs several validations:
     * <ul>
     *   <li>Table must not be already occupied (throws exception if OCCUPIED)</li>
     *   <li>Price per hour must be positive (throws exception if zero or negative)</li>
     *   <li>Catches and handles unexpected exceptions during repository operations</li>
     * </ul>
     *
     * @param tableCode The unique code of the billiard table to update.
     * @param newPricePerHour The new hourly rate for the table.
     * @return A {@code BilliardTableResponseDto} containing the updated table data with new price.
     * @throws ResourceNotFoundException if the table is occupied, price is invalid, or no table exists.
     */
    @Transactional
    public BilliardTableResponseDto updateTablePrice(String tableCode, BigDecimal newPricePerHour) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
                .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));
        table.setPricePerHour(newPricePerHour);
        if (table.getTableStatus() == TableStatus.OCCUPIED) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("table_in_use"));
        }

        if (newPricePerHour.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("price_must_be_positive"));
        }

        try {
            BilliardTable updatedTable = billiardTableRepository.save(table);
            return convertToResponseDto(updatedTable);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to update table price");
        }
    }

    /**
     * Marks a billiard table as deleted using soft-delete pattern.
     *
     * <p>This method logically deletes the specified table by setting its isDeleted flag to
     * true in the database. Unlike hard delete, this operation preserves historical data for
     * audit trails while preventing future use of the record. It then saves the updated entity.
     *
     * @param tableCode The unique code of the billiard table to mark as deleted.
     */
    public void delete(String tableCode) {
        setTableToDelete(tableCode);
    }

    /**
     * Updates the status of a specific billiard table.
     *
     * <p>This method modifies the occupancy or availability state of a table by setting it
     * to a new {@link TableStatus}. It enforces several business rules:
     * <ul>
     *   <li>Cannot set status if table doesn't exist (throws exception)</li>
     *   <li>Cannot change from AVAILABLE to MAINTENANCE directly</li>
     *   <li>Cannot change from OCCUPIED to MAINTENANCE (table must be free first)</li>
     *   <li>Cannot set status to the same value as current (throws exception if equal)</li>
     * </ul>
     *
     * @param tableCode The unique code of the billiard table.
     * @param newStatus The desired new status for the table.
     * @return A {@code BilliardTableResponseDto} containing the updated table with new status value.
     * @throws ResourceNotFoundException if the table is occupied while trying to set maintenance,
     *         status doesn't change, or table does not exist.
     */
    public BilliardTableResponseDto setStatusTable(String tableCode, TableStatus newStatus) {
        BilliardTable table = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
                .orElseThrow(() -> new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found")));

        if (table.getTableStatus() == newStatus) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found"));
        }

        if (table.getTableStatus() == TableStatus.MAINTENANCE && newStatus == TableStatus.OCCUPIED) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("table_not_found"));
        }

        if (table.getTableStatus() == TableStatus.OCCUPIED && newStatus == TableStatus.MAINTENANCE) {
            throw new ResourceNotFoundException(SystemLanguage.getMessage("table_in_use"));
        }


        table.setTableStatus(newStatus);
        BilliardTable savedTable = billiardTableRepository.save(table);
        return convertToResponseDto(savedTable);
    }

    /**
     * Transforms a {@link BilliardTable} entity into a response DTO.
     *
     * <p>This private method maps all relevant fields from the entity to the corresponding
     * {@link BilliardTableResponseDto}, including metadata fields such as creation and update
     * timestamps, user IDs, and last modification time.</p>
     *
     * @param table The billiard table entity to convert.
     * @return A populated {@link BilliardTableResponseDto} with all field values from the entity.
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
     * Sets a billiard table as soft-deleted.
     *
     * <p>Private helper method used by {@link #delete(String)} to perform the actual
     * soft-delete operation. It finds the target table and sets its isDeleted flag to true,
     * then saves the updated entity to persist the change.</p>
     *
     * @param tableCode The unique code of the billiard table to mark as deleted.
     */
    private void setTableToDelete(String tableCode) {
        BilliardTable targetTable = billiardTableRepository.findByTableCodeAndIsDeletedFalse(tableCode)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        targetTable.setIsDeleted(true);
        billiardTableRepository.save(targetTable);
    }
}
