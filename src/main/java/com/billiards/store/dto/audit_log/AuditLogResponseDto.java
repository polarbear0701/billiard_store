package com.billiards.store.dto.audit_log;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private UUID id;
    private LocalDateTime occurredAt;
    private String actor;
    private String action;
    private String entityType;
    private String entityCode;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
}
