package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.BilliardTable;

public interface BilliardTableRepository extends JpaRepository<BilliardTable, UUID> {
    Optional<BilliardTable> findByTableCodeAndIsDeletedFalse(String tableCode);
    Optional<BilliardTable> findByIdAndIsDeletedFalse(UUID id);
    List<BilliardTable> findByIsDeletedFalse();
}
