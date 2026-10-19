package com.billiards.store.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.dto.audit_log.AuditLogResponseDto;
import com.billiards.store.model.AuditLog;
import com.billiards.store.repository.AuditLogRepository;

import jakarta.persistence.criteria.Predicate;

/**
 * Service for querying audit log entries.
 * Writing entries is handled by {@link AuditService}.
 */
@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	/**
	 * Searches audit log entries. Every filter is optional; only the non-null ones are applied.
	 *
	 * @param actor username that performed the action
	 * @param entityType type of the affected entity (e.g. Product)
	 * @param entityCode business code of the affected entity
	 * @param action action name (e.g. CREATE, DELETE)
	 * @param from inclusive lower bound of the occurrence time
	 * @param to exclusive upper bound of the occurrence time
	 * @param pageable the pagination and sorting information
	 * @return a paginated response of matching audit entries
	 */
	public PagedResponse<AuditLogResponseDto> search(
		String actor,
		String entityType,
		String entityCode,
		String action,
		LocalDateTime from,
		LocalDateTime to,
		Pageable pageable
	) {
		Specification<AuditLog> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (actor != null) {
				predicates.add(cb.equal(root.get("actor"), actor));
			}
			if (entityType != null) {
				predicates.add(cb.equal(root.get("entityType"), entityType));
			}
			if (entityCode != null) {
				predicates.add(cb.equal(root.get("entityCode"), entityCode));
			}
			if (action != null) {
				predicates.add(cb.equal(root.get("action"), action));
			}
			if (from != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
			}
			if (to != null) {
				predicates.add(cb.lessThan(root.get("occurredAt"), to));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};

		return PagedResponse.from(
			auditLogRepository.findAll(spec, pageable).map(this::convertToResponseDto)
		);
	}

	private AuditLogResponseDto convertToResponseDto(AuditLog log) {
		return new AuditLogResponseDto(
			log.getId(),
			log.getOccurredAt(),
			log.getActor(),
			log.getAction(),
			log.getEntityType(),
			log.getEntityCode(),
			log.getOldValue(),
			log.getNewValue()
		);
	}
}
