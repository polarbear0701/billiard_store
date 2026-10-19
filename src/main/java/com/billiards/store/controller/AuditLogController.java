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
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/audit-logs")
@Tag(name = "Audit Log", description = "Read-only access to the audit trail (admin only)")
public class AuditLogController {
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "Search audit log entries, newest first")
    public ResponseEntity<SuccessResponse<?>> search(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityCode,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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
