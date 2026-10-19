package com.billiards.store.dto.audit_log;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "One audit log entry")
public class AuditLogResponseDto {
    @Schema(description = "Unique id of the entry", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "When the action happened (ISO-8601 date-time in the store's timezone)", example = "2026-10-19T18:30:00")
    private LocalDateTime occurredAt;
    @Schema(description = "Username that did it; `SYSTEM` if there was no logged-in user", example = "admin")
    private String actor;
    @Schema(description = "What was done: CREATE, UPDATE, DELETE, END, COMPLETE, CANCEL, UPDATE_STOCK, UPDATE_PRICE, UPDATE_NAME, CLEAR_STOCK, UPDATE_STATUS, UPDATE_QUANTITY, ADJUST_START_TIME", example = "UPDATE_PRICE")
    private String action;
    @Schema(description = "Kind of record: Product, BilliardTable, PlaySession, OrderItem or Invoice", example = "Product")
    private String entityType;
    @Schema(description = "Code of the affected record", example = "PRD000001")
    private String entityCode;
    @Schema(description = "Snapshot of the record before the action (same fields as its response DTO); null for create")
    private Map<String, Object> oldValue;
    @Schema(description = "Snapshot of the record after the action; null for delete")
    private Map<String, Object> newValue;
}
