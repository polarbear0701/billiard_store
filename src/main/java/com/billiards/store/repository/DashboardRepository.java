package com.billiards.store.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.billiards.store.model.Invoice;

/**
 * Read-only aggregate queries for the dashboard (PostgreSQL).
 * Revenue means completed, non-deleted invoices, counted at the time they were paid.
 * All time ranges are half-open: {@code start <= t < end}.
 */
public interface DashboardRepository extends Repository<Invoice, UUID> {

	interface RevenueTotals {
		BigDecimal getTotal();
		BigDecimal getTableFee();
		BigDecimal getFnbFee();
		Long getInvoiceCount();
	}

	interface RevenueBucket {
		LocalDateTime getBucket();
		BigDecimal getTotal();
		BigDecimal getTableFee();
		BigDecimal getFnbFee();
		Long getInvoiceCount();
	}

	interface ProductSales {
		String getProductCode();
		String getName();
		Long getQuantity();
		BigDecimal getRevenue();
	}

	interface TableUsage {
		String getTableCode();
		Long getSessionCount();
		Double getPlayedMinutes();
	}

	interface TableRevenue {
		String getTableCode();
		BigDecimal getRevenue();
	}

	@Query(
		value = """
			SELECT COALESCE(SUM(total_amount), 0) AS "total",
			       COALESCE(SUM(table_fee), 0)    AS "tableFee",
			       COALESCE(SUM(fnb_fee), 0)      AS "fnbFee",
			       COUNT(*)                       AS "invoiceCount"
			FROM invoice
			WHERE complete = TRUE AND is_deleted = FALSE
			  AND paid_at >= :start AND paid_at < :end
			""",
		nativeQuery = true
	)
	RevenueTotals totals(
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	/**
	 * Revenue grouped by {@code date_trunc(unit, paid_at)}; {@code unit} must come from {@code Granularity}.
	 */
	@Query(
		value = """
			SELECT date_trunc(:unit, paid_at) AS "bucket",
			       SUM(total_amount)          AS "total",
			       SUM(table_fee)             AS "tableFee",
			       SUM(fnb_fee)               AS "fnbFee",
			       COUNT(*)                   AS "invoiceCount"
			FROM invoice
			WHERE complete = TRUE AND is_deleted = FALSE
			  AND paid_at >= :start AND paid_at < :end
			GROUP BY 1
			ORDER BY 1
			""",
		nativeQuery = true
	)
	List<RevenueBucket> revenueByBucket(
		@Param("unit") String unit,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	/**
	 * Products sold on invoices paid in the range. Cancelled order lines (soft-deleted) are excluded.
	 */
	@Query(
		value = """
			SELECT oi.product_code               AS "productCode",
			       COALESCE(MAX(p.name), oi.product_code) AS "name",
			       SUM(oi.quantity)              AS "quantity",
			       SUM(oi.quantity * oi.price)   AS "revenue"
			FROM order_item oi
			JOIN invoice i ON i.session_code = oi.session_code
			LEFT JOIN product p ON p.product_code = oi.product_code
			WHERE oi.is_deleted = FALSE
			  AND i.complete = TRUE AND i.is_deleted = FALSE
			  AND i.paid_at >= :start AND i.paid_at < :end
			GROUP BY oi.product_code
			""",
		nativeQuery = true
	)
	List<ProductSales> productSales(
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	/**
	 * Minutes each table was in use inside the range. Sessions are clipped to the range, and
	 * sessions still running are counted up to {@code now}.
	 */
	@Query(
		value = """
			SELECT table_code AS "tableCode",
			       COUNT(*)   AS "sessionCount",
			       SUM(EXTRACT(EPOCH FROM (LEAST(COALESCE(end_time, :now), :end) - GREATEST(start_time, :start))) / 60.0)
			                  AS "playedMinutes"
			FROM play_session
			WHERE is_deleted = FALSE
			  AND session_status IN ('ACTIVE', 'COMPLETED')
			  AND start_time IS NOT NULL
			  AND start_time < :end
			  AND COALESCE(end_time, :now) > :start
			GROUP BY table_code
			""",
		nativeQuery = true
	)
	List<TableUsage> tableUsage(
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		@Param("now") LocalDateTime now
	);

	/**
	 * Table fees per table on invoices paid in the range.
	 */
	@Query(
		value = """
			SELECT s.table_code       AS "tableCode",
			       SUM(i.table_fee)   AS "revenue"
			FROM invoice i
			JOIN play_session s ON s.session_code = i.session_code
			WHERE i.complete = TRUE AND i.is_deleted = FALSE
			  AND i.paid_at >= :start AND i.paid_at < :end
			GROUP BY s.table_code
			""",
		nativeQuery = true
	)
	List<TableRevenue> tableRevenue(
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);
}
