package com.billiards.store.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.billiards.store.model.Invoice;

@Repository
public interface FinancialReportRepository extends JpaRepository<Invoice, UUID> {
    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoice i WHERE CAST(i.created_at AS date) = CAST(:date AS date)", nativeQuery = true)
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoice i WHERE CAST(i.created_at AS date) BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)", nativeQuery = true)
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoice i WHERE EXTRACT(YEAR FROM i.created_at) = :year", nativeQuery = true)
    BigDecimal getTotalRevenueByYear(@Param("year") int year);
}
