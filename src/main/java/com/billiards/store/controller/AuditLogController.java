package com.billiards.store.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.audit_log.AuditLogResponseDto;
import com.billiards.store.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/audit-logs")
@Tag(
    name = "Audit Log",
    description = "Read-only history of who changed what. ADMIN only. Entries are written automatically by the server and cannot be edited or deleted."
)
public class AuditLogController {
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(
        operationId = "auditLog_search",
        summary = "Search the audit log",
        description = "Returns audit entries newest first. All filters are optional and combine with AND. "
            + "Typical uses: the history of one record (`entityType` + `entityCode`), or everything one person did (`actor`). "
            + "`oldValue` and `newValue` hold a snapshot of the record before and after the action (null for create / delete). "
            + "The page size is capped at 100. Access: ADMIN only."
    )
    public ResponseEntity<SuccessResponse<PagedResponse<AuditLogResponseDto>>> search(
            @Parameter(description = "Username that performed the action; `SYSTEM` for actions without a logged-in user", example = "admin")
            @RequestParam(required = false) String actor,
            @Parameter(description = "Type of the affected record: Product, BilliardTable, PlaySession, OrderItem or Invoice", example = "Product")
            @RequestParam(required = false) String entityType,
            @Parameter(description = "Code of the affected record, e.g. a product, table, session, order line or invoice code", example = "PRD000001")
            @RequestParam(required = false) String entityCode,
            @Parameter(description = "What was done, e.g. CREATE, UPDATE, DELETE, END, COMPLETE, UPDATE_STOCK, UPDATE_PRICE, UPDATE_STATUS, CANCEL", example = "UPDATE")
            @RequestParam(required = false) String action,
            @Parameter(description = "Only entries at or after this time (ISO date-time, store timezone)", example = "2026-10-19T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Only entries before this time (ISO date-time, store timezone)", example = "2026-10-20T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, 1 to 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by time: `desc` (newest first, default) or `asc`", example = "desc")
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), "occurredAt");
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);
        PagedResponse<AuditLogResponseDto> logs = auditLogService.search(
                actor, entityType, entityCode, action, from, to, pageable);
        return ResponseEntity.ok(
                new SuccessResponse<>(
                        HttpStatus.OK.value(),
                        SystemLanguage.getMessage("success"),
                        logs,
                        LocalDateTime.now()));
    }
}
