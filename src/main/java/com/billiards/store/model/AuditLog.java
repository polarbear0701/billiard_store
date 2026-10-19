package com.billiards.store.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only record of an action performed in the system.
 * Intentionally does not extend BaseEntity: audit rows are never updated or soft-deleted.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "audit_log")
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, updatable = false)
	private LocalDateTime occurredAt;

	@Column(nullable = false, updatable = false)
	private String actor;

	@Column(nullable = false, updatable = false)
	private String action;

	@Column(nullable = false, updatable = false)
	private String entityType;

	@Column(updatable = false)
	private String entityCode;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", updatable = false)
	private Map<String, Object> oldValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", updatable = false)
	private Map<String, Object> newValue;

	@Builder
	private AuditLog(String actor, String action, String entityType, String entityCode,
			Map<String, Object> oldValue, Map<String, Object> newValue) {
		this.actor = actor;
		this.action = action;
		this.entityType = entityType;
		this.entityCode = entityCode;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@PrePersist
	void onCreate() {
		if (occurredAt == null) {
			occurredAt = LocalDateTime.now();
		}
	}
}
