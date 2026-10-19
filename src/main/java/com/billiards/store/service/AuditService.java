package com.billiards.store.service;

import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.billiards.store.model.AuditLog;
import com.billiards.store.repository.AuditLogRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Service for recording audit log entries.
 * Entries join the caller's transaction, so no entry is kept if the audited action rolls back.
 */
@Service
public class AuditService {

	private static final String SYSTEM_USER = "SYSTEM";

	private final AuditLogRepository auditLogRepository;
	private final ObjectMapper objectMapper;

	public AuditService(
		AuditLogRepository auditLogRepository,
		ObjectMapper objectMapper
	) {
		this.auditLogRepository = auditLogRepository;
		this.objectMapper = objectMapper;
	}

	/**
	 * Records an audit entry for the current user.
	 * Pass DTOs rather than JPA entities, and never include secrets such as password hashes or tokens.
	 *
	 * @param action The action performed (e.g. CREATE, UPDATE, DELETE)
	 * @param entityType The type of entity affected (e.g. Product)
	 * @param entityCode The business code of the affected entity
	 * @param oldValue The state before the action, or null
	 * @param newValue The state after the action, or null
	 */
	@Transactional
	public void record(
		String action,
		String entityType,
		String entityCode,
		Object oldValue,
		Object newValue
	) {
		auditLogRepository.save(
			AuditLog.builder()
				.actor(currentActor())
				.action(action)
				.entityType(entityType)
				.entityCode(entityCode)
				.oldValue(toMap(oldValue))
				.newValue(toMap(newValue))
				.build()
		);
	}

	private String currentActor() {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();
		if (
			authentication == null ||
			!authentication.isAuthenticated() ||
			authentication instanceof AnonymousAuthenticationToken
		) {
			return SYSTEM_USER;
		}
		return authentication.getName();
	}

	private Map<String, Object> toMap(Object value) {
		if (value == null) {
			return null;
		}
		return objectMapper.convertValue(
			value,
			new TypeReference<Map<String, Object>>() {}
		);
	}
}
