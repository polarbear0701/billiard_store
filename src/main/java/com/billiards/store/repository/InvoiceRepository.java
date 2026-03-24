package com.billiards.store.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billiards.store.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceCodeAndIsDeletedFalse(String invoiceCode);
    Optional<Invoice> findByIdAndIsDeletedFalse(UUID id);
    List<Invoice> findByIsDeletedFalse();
    boolean existsBySessionCodeAndIsDeletedFalse(String sessionCode);

    @Query(value = "SELECT to_char(created_at, 'HH24:00') as label, SUM(total_amount) as value " +
                "FROM invoice " +
                "WHERE created_at BETWEEN :start AND :end AND is_deleted = false " +
                "GROUP BY to_char(created_at, 'HH24:00') " +
                "ORDER BY label", nativeQuery = true)
    List<Object[]> findHourlyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT to_char(created_at, 'YYYY-MM-DD') as label, SUM(total_amount) as value " +
                "FROM invoice " +
                "WHERE created_at BETWEEN :start AND :end AND is_deleted = false " +
                "GROUP BY to_char(created_at, 'YYYY-MM-DD') " +
                "ORDER BY label", nativeQuery = true)
    List<Object[]> findDailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT to_char(created_at, 'YYYY-MM') as label, SUM(total_amount) as value " +
                "FROM invoice " +
                "WHERE created_at BETWEEN :start AND :end AND is_deleted = false " +
                "GROUP BY to_char(created_at, 'YYYY-MM') " +
                "ORDER BY label", nativeQuery = true)
    List<Object[]> findMonthlyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
