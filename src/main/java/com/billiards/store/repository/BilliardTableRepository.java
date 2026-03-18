package com.billiards.store.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.BilliardTable;

public interface BilliardTableRepository extends JpaRepository<BilliardTable, UUID> {
    Optional<BilliardTable> findByTableCode(String tableCode);
}
