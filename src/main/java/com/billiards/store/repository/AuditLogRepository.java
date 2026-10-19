package com.billiards.store.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.billiards.store.model.AuditLog;

/**
 * Repository for audit log entries.
 * Specification support is included for the filtered listing endpoint (actor, entity, action, date range).
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
}
