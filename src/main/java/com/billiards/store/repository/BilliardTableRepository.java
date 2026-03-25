package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.model.BilliardTable;

public interface BilliardTableRepository extends JpaRepository<BilliardTable, UUID> {
    Optional<BilliardTable> findByTableCodeAndIsDeletedFalse(String tableCode);
    Optional<BilliardTable> findByIdAndIsDeletedFalse(UUID id);
    List<BilliardTable> findByIsDeletedFalse();

    @Query("SELECT b.tableStatus FROM BilliardTable b WHERE b.tableCode = :tableCode")
    Optional<TableStatus> findStatusByTableCode(@Param("tableCode") String tableCode);
}
