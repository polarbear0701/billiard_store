package com.billiards.store.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.enumerated.Granularity;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.dashboard.LowStockProductDto;
import com.billiards.store.dto.dashboard.RevenueSeriesDto;
import com.billiards.store.dto.dashboard.RevenueSummaryDto;
import com.billiards.store.dto.dashboard.TableOverviewDto;
import com.billiards.store.dto.dashboard.TopProductDto;
import com.billiards.store.service.DashboardService;
import com.billiards.store.service.DashboardService.ProductRanking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(
	name = "Dashboard",
	description = "Revenue, product and table analytics. ADMIN only. " +
		"Dates are calendar days (`yyyy-MM-dd`), both ends inclusive, in the store's timezone. " +
		"Revenue counts only invoices that are marked paid, on the day they were paid. " +
		"A range may span at most 366 days (31 days for hourly revenue); otherwise the API answers 400."
)
public class DashboardController {

	private static final String DATE_FROM = "First day of the period, inclusive (`yyyy-MM-dd`)";
	private static final String DATE_TO = "Last day of the period, inclusive (`yyyy-MM-dd`)";

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	private <T> ResponseEntity<SuccessResponse<T>> ok(T data) {
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				data,
				LocalDateTime.now()
			)
		);
	}

	@GetMapping("/summary")
	@Operation(
		operationId = "dashboard_summary",
		summary = "Revenue summary for a period",
		description = "Total revenue split into table fees and food and drink, invoice count and average invoice, " +
			"compared with the period of the same length directly before it. " +
			"`from` and `to` both default to today. Access: ADMIN only."
	)
	public ResponseEntity<SuccessResponse<RevenueSummaryDto>> summary(
		@Parameter(description = DATE_FROM + ". Default: today", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@Parameter(description = DATE_TO + ". Default: today", example = "2026-10-19") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ok(dashboardService.getSummary(from, to));
	}

	@GetMapping("/revenue")
	@Operation(
		operationId = "dashboard_revenue",
		summary = "Revenue over time (chart data)",
		description = "One point per hour, day or month between `from` and `to`. Periods with no sales are included with zeros, " +
			"so the result can be plotted directly. Months are calendar months. " +
			"Default: the last 30 days by day. Hourly granularity is limited to 31 days. Access: ADMIN only."
	)
	public ResponseEntity<SuccessResponse<RevenueSeriesDto>> revenue(
		@Parameter(description = DATE_FROM + ". Default: 29 days before `to`", example = "2026-09-20") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@Parameter(description = DATE_TO + ". Default: today", example = "2026-10-19") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@Parameter(description = "Bucket size. Default: DAY") @RequestParam(required = false) Granularity granularity
	) {
		return ok(dashboardService.getRevenueSeries(from, to, granularity));
	}

	@GetMapping("/products/top")
	@Operation(
		operationId = "dashboard_topProducts",
		summary = "Best-selling products",
		description = "Products sold on invoices paid in the period, ranked by revenue or by quantity. " +
			"Cancelled order lines and unpaid invoices are not counted. `from` and `to` both default to today. Access: ADMIN only."
	)
	public ResponseEntity<SuccessResponse<List<TopProductDto>>> topProducts(
		@Parameter(description = DATE_FROM + ". Default: today", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@Parameter(description = DATE_TO + ". Default: today", example = "2026-10-19") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@Parameter(description = "Ranking criterion") @RequestParam(defaultValue = "REVENUE") ProductRanking by,
		@Parameter(description = "How many products to return, 1 to 50", example = "10") @RequestParam(defaultValue = "10") int limit
	) {
		return ok(dashboardService.getTopProducts(from, to, by, limit));
	}

	@GetMapping("/products/low-stock")
	@Operation(
		operationId = "dashboard_lowStock",
		summary = "Products that need restocking",
		description = "Products whose stock is at or below the threshold, lowest stock first. This is the current state, not tied to a period. Access: ADMIN only."
	)
	public ResponseEntity<SuccessResponse<List<LowStockProductDto>>> lowStock(
		@Parameter(description = "Include products with at most this many units in stock (0 or more)", example = "5") @RequestParam(defaultValue = "5") int threshold
	) {
		return ok(dashboardService.getLowStock(threshold));
	}

	@GetMapping("/tables")
	@Operation(
		operationId = "dashboard_tables",
		summary = "Table usage and revenue",
		description = "Current number of tables per status, plus for every table the sessions, minutes played, utilization and table revenue in the period. " +
			"Play time is cut to the requested days, and sessions still running count up to now. " +
			"Utilization is minutes played divided by the opening hours of the period (24 h a day unless the server is configured otherwise). " +
			"`from` and `to` both default to today. Access: ADMIN only."
	)
	public ResponseEntity<SuccessResponse<TableOverviewDto>> tables(
		@Parameter(description = DATE_FROM + ". Default: today", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@Parameter(description = DATE_TO + ". Default: today", example = "2026-10-19") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ok(dashboardService.getTableOverview(from, to));
	}
}
