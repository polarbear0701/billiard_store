package com.billiards.store.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
import com.billiards.store.service.DashboardService;
import com.billiards.store.service.DashboardService.ProductRanking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin-only analytics. Dates are inclusive days (yyyy-MM-dd) in the store's timezone.
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Revenue, product and table analytics (admin only)")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	private ResponseEntity<SuccessResponse<?>> ok(Object data) {
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
	@Operation(summary = "Revenue totals for a period, compared with the previous period (default: today)")
	public ResponseEntity<SuccessResponse<?>> summary(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ok(dashboardService.getSummary(from, to));
	}

	@GetMapping("/revenue")
	@Operation(summary = "Revenue time series by hour, day or month (default: last 30 days by day)")
	public ResponseEntity<SuccessResponse<?>> revenue(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(required = false) Granularity granularity
	) {
		return ok(dashboardService.getRevenueSeries(from, to, granularity));
	}

	@GetMapping("/products/top")
	@Operation(summary = "Best-selling products in a period (default: today)")
	public ResponseEntity<SuccessResponse<?>> topProducts(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(defaultValue = "REVENUE") ProductRanking by,
		@RequestParam(defaultValue = "10") int limit
	) {
		return ok(dashboardService.getTopProducts(from, to, by, limit));
	}

	@GetMapping("/products/low-stock")
	@Operation(summary = "Products at or below a stock threshold")
	public ResponseEntity<SuccessResponse<?>> lowStock(
		@RequestParam(defaultValue = "5") int threshold
	) {
		return ok(dashboardService.getLowStock(threshold));
	}

	@GetMapping("/tables")
	@Operation(summary = "Current table statuses plus usage and revenue per table for a period (default: today)")
	public ResponseEntity<SuccessResponse<?>> tables(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ok(dashboardService.getTableOverview(from, to));
	}
}
