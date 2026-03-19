package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.PlaySession;

public interface PlaySessionRepository extends JpaRepository<PlaySession, UUID> {
    Optional<PlaySession> findBySessionCodeAndIsDeletedFalse(String sessionCode);
    Optional<PlaySession> findByIdAndIsDeletedFalse(UUID id);
    List<PlaySession> findByIsDeletedFalse();
}
