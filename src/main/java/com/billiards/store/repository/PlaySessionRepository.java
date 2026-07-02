package com.billiards.store.repository;

import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.model.PlaySession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for PlaySession entity operations.
 * Provides methods for finding play sessions, checking existence, and querying by table/status.
 */
public interface PlaySessionRepository
	extends JpaRepository<PlaySession, UUID>
{
	/**
	 * Finds a play session by its code if it exists and is not deleted.
	 * @param sessionCode The unique session code
	 * @return Optional containing the play session if found and not deleted, or empty otherwise
	 */
	Optional<PlaySession> findBySessionCodeAndIsDeletedFalse(
		String sessionCode
	);

	/**
	 * Finds a play session by its ID if it exists and is not deleted.
	 * @param id The unique session identifier
	 * @return Optional containing the play session if found and not deleted, or empty otherwise
	 */
	Optional<PlaySession> findByIdAndIsDeletedFalse(UUID id);

	/**
	 * Returns all play sessions that are not marked as deleted.
	 * @return List of all non-deleted play sessions
	 */
	List<PlaySession> findByIsDeletedFalse();

	/**
	 * Returns a page of play sessions that are not marked as deleted.
	 * @param pageable the pagination and sorting information
	 * @return a page of non-deleted play sessions
	 */
	Page<PlaySession> findByIsDeletedFalse(Pageable pageable);

	/**
	 * Finds a play session by table code and session status if it exists and is not deleted.
	 * @param tableCode The table code to filter by
	 * @param sessionStatus The required session status
	 * @return Optional containing the play session if found and not deleted, or empty otherwise
	 */
	Optional<PlaySession> findByTableCodeAndSessionStatusAndIsDeletedFalse(
		String tableCode,
		SessionStatus sessionStatus
	);
}
