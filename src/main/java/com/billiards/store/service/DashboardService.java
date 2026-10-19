package com.billiards.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.billiards.store.common.enumerated.Granularity;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.exception.BadRequestException;
import com.billiards.store.dto.dashboard.LowStockProductDto;
import com.billiards.store.dto.dashboard.RevenuePointDto;
import com.billiards.store.dto.dashboard.RevenueSeriesDto;
import com.billiards.store.dto.dashboard.RevenueSummaryDto;
import com.billiards.store.dto.dashboard.TableOverviewDto;
import com.billiards.store.dto.dashboard.TableUtilizationDto;
import com.billiards.store.dto.dashboard.TopProductDto;
import com.billiards.store.model.BilliardTable;
import com.billiards.store.repository.BilliardTableRepository;
import com.billiards.store.repository.DashboardRepository;
import com.billiards.store.repository.DashboardRepository.ProductSales;
import com.billiards.store.repository.DashboardRepository.RevenueBucket;
import com.billiards.store.repository.DashboardRepository.RevenueTotals;
import com.billiards.store.repository.DashboardRepository.TableRevenue;
import com.billiards.store.repository.DashboardRepository.TableUsage;
import com.billiards.store.repository.ProductRepository;

/**
 * Service for dashboard analytics.
 * Dates are inclusive calendar days in the store's timezone; internally they become the half-open range
 * [from 00:00, to+1 00:00), which avoids end-of-day edge cases.
 */
@Service
public class DashboardService {

	public static final int MAX_RANGE_DAYS = 366;
	public static final int MAX_HOURLY_RANGE_DAYS = 31;
	public static final int MAX_TOP_LIMIT = 50;
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	/** Inclusive dates plus the matching half-open timestamps. */
	private record Range(
		LocalDate from,
		LocalDate to,
		LocalDateTime start,
		LocalDateTime end
	) {
		long days() {
			return ChronoUnit.DAYS.between(from, to) + 1;
		}
	}

	public enum ProductRanking {
		REVENUE,
		QUANTITY,
	}

	private final DashboardRepository dashboardRepository;
	private final ProductRepository productRepository;
	private final BilliardTableRepository billiardTableRepository;
	private final Clock clock;
	private final int openHoursPerDay;

	public DashboardService(
		DashboardRepository dashboardRepository,
		ProductRepository productRepository,
		BilliardTableRepository billiardTableRepository,
		Clock clock,
		@Value("${app.dashboard.open-hours-per-day:24}") int openHoursPerDay
	) {
		this.dashboardRepository = dashboardRepository;
		this.productRepository = productRepository;
		this.billiardTableRepository = billiardTableRepository;
		this.clock = clock;
		this.openHoursPerDay = Math.min(Math.max(openHoursPerDay, 1), 24);
	}

	/**
	 * Revenue totals for a period, compared with the period of the same length right before it.
	 *
	 * @param from first day, inclusive (default: today)
	 * @param to last day, inclusive (default: today)
	 */
	public RevenueSummaryDto getSummary(LocalDate from, LocalDate to) {
		LocalDate today = LocalDate.now(clock);
		Range range = resolve(from == null ? today : from, to == null ? today : to);

		LocalDate previousTo = range.from().minusDays(1);
		LocalDate previousFrom = previousTo.minusDays(range.days() - 1);
		Range previousRange = toRange(previousFrom, previousTo);

		RevenueTotals current = dashboardRepository.totals(range.start(), range.end());
		RevenueTotals previous = dashboardRepository.totals(
			previousRange.start(),
			previousRange.end()
		);

		long count = current.getInvoiceCount();
		BigDecimal average = count == 0
			? BigDecimal.ZERO.setScale(2)
			: current.getTotal().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
		BigDecimal changePercent = previous.getTotal().signum() == 0
			? null
			: current
				.getTotal()
				.subtract(previous.getTotal())
				.multiply(HUNDRED)
				.divide(previous.getTotal(), 1, RoundingMode.HALF_UP);

		return new RevenueSummaryDto(
			range.from(),
			range.to(),
			current.getTotal(),
			current.getTableFee(),
			current.getFnbFee(),
			count,
			average,
			new RevenueSummaryDto.PreviousPeriod(
				previousFrom,
				previousTo,
				previous.getTotal(),
				previous.getInvoiceCount()
			),
			changePercent
		);
	}

	/**
	 * Revenue time series with empty buckets filled with zeros.
	 *
	 * @param from first day, inclusive (default: 29 days before {@code to})
	 * @param to last day, inclusive (default: today)
	 * @param granularity HOUR, DAY or MONTH (default: DAY)
	 */
	public RevenueSeriesDto getRevenueSeries(
		LocalDate from,
		LocalDate to,
		Granularity granularity
	) {
		Granularity unit = granularity == null ? Granularity.DAY : granularity;
		LocalDate effectiveTo = to == null ? LocalDate.now(clock) : to;
		LocalDate effectiveFrom = from == null ? effectiveTo.minusDays(29) : from;
		Range range = resolve(effectiveFrom, effectiveTo);
		if (unit == Granularity.HOUR && range.days() > MAX_HOURLY_RANGE_DAYS) {
			throw new BadRequestException(
				"Hourly revenue supports at most " + MAX_HOURLY_RANGE_DAYS + " days"
			);
		}

		Map<LocalDateTime, RevenueBucket> byBucket = new HashMap<>();
		for (RevenueBucket bucket : dashboardRepository.revenueByBucket(
			unit.getSqlUnit(),
			range.start(),
			range.end()
		)) {
			byBucket.put(bucket.getBucket(), bucket);
		}

		List<RevenuePointDto> points = new ArrayList<>();
		LocalDateTime cursor = firstBucket(range, unit);
		while (cursor.isBefore(range.end())) {
			RevenueBucket bucket = byBucket.get(cursor);
			points.add(
				bucket == null
					? new RevenuePointDto(cursor, zero(), zero(), zero(), 0)
					: new RevenuePointDto(
						cursor,
						bucket.getTotal(),
						bucket.getTableFee(),
						bucket.getFnbFee(),
						bucket.getInvoiceCount()
					)
			);
			cursor = nextBucket(cursor, unit);
		}
		return new RevenueSeriesDto(range.from(), range.to(), unit, points);
	}

	/**
	 * Best-selling products on invoices paid in the period.
	 *
	 * @param from first day, inclusive (default: today)
	 * @param to last day, inclusive (default: today)
	 * @param by rank by REVENUE (default) or QUANTITY
	 * @param limit number of products, 1 to {@value #MAX_TOP_LIMIT}
	 */
	public List<TopProductDto> getTopProducts(
		LocalDate from,
		LocalDate to,
		ProductRanking by,
		int limit
	) {
		if (limit < 1 || limit > MAX_TOP_LIMIT) {
			throw new BadRequestException(
				"limit must be between 1 and " + MAX_TOP_LIMIT
			);
		}
		LocalDate today = LocalDate.now(clock);
		Range range = resolve(from == null ? today : from, to == null ? today : to);

		Comparator<ProductSales> comparator = by == ProductRanking.QUANTITY
			? Comparator.comparing(ProductSales::getQuantity)
			: Comparator.comparing(ProductSales::getRevenue);
		return dashboardRepository
			.productSales(range.start(), range.end())
			.stream()
			.sorted(
				comparator
					.reversed()
					.thenComparing(ProductSales::getProductCode)
			)
			.limit(limit)
			.map(p ->
				new TopProductDto(
					p.getProductCode(),
					p.getName(),
					p.getQuantity(),
					p.getRevenue()
				)
			)
			.toList();
	}

	/**
	 * Products whose stock is at or below the threshold, lowest first.
	 */
	public List<LowStockProductDto> getLowStock(int threshold) {
		if (threshold < 0) {
			throw new BadRequestException("threshold cannot be negative");
		}
		return productRepository
			.findByQuantityLessThanEqualAndIsDeletedFalseOrderByQuantityAscNameAsc(
				threshold
			)
			.stream()
			.map(p ->
				new LowStockProductDto(p.getProductCode(), p.getName(), p.getQuantity())
			)
			.toList();
	}

	/**
	 * Current table statuses plus per-table usage and table revenue for the period.
	 * Utilization is played minutes divided by (days x opening hours), capped at 100%.
	 *
	 * @param from first day, inclusive (default: today)
	 * @param to last day, inclusive (default: today)
	 */
	public TableOverviewDto getTableOverview(LocalDate from, LocalDate to) {
		LocalDate today = LocalDate.now(clock);
		Range range = resolve(from == null ? today : from, to == null ? today : to);
		LocalDateTime now = LocalDateTime.now(clock);

		Map<String, TableUsage> usage = new HashMap<>();
		dashboardRepository
			.tableUsage(range.start(), range.end(), now)
			.forEach(u -> usage.put(u.getTableCode(), u));
		Map<String, BigDecimal> revenue = new HashMap<>();
		for (TableRevenue r : dashboardRepository.tableRevenue(
			range.start(),
			range.end()
		)) {
			revenue.put(r.getTableCode(), r.getRevenue());
		}

		BigDecimal capacityMinutes = BigDecimal.valueOf(
			range.days() * openHoursPerDay * 60L
		);
		Map<TableStatus, Long> statusCounts = new EnumMap<>(TableStatus.class);
		for (TableStatus status : TableStatus.values()) {
			statusCounts.put(status, 0L);
		}

		List<TableUtilizationDto> tables = new ArrayList<>();
		List<BilliardTable> allTables = billiardTableRepository
			.findByIsDeletedFalse()
			.stream()
			.sorted(Comparator.comparing(BilliardTable::getTableCode))
			.toList();
		for (BilliardTable table : allTables) {
			statusCounts.merge(table.getTableStatus(), 1L, Long::sum);

			TableUsage u = usage.get(table.getTableCode());
			long minutes = u == null ? 0 : Math.round(u.getPlayedMinutes());
			BigDecimal percent = BigDecimal.valueOf(minutes)
				.multiply(HUNDRED)
				.divide(capacityMinutes, 1, RoundingMode.HALF_UP)
				.min(HUNDRED);
			tables.add(
				new TableUtilizationDto(
					table.getTableCode(),
					table.getTableType(),
					table.getTableStatus(),
					u == null ? 0 : u.getSessionCount(),
					minutes,
					percent,
					revenue.getOrDefault(table.getTableCode(), zero())
				)
			);
		}
		return new TableOverviewDto(range.from(), range.to(), statusCounts, tables);
	}

	private Range resolve(LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new BadRequestException("from must not be after to");
		}
		Range range = toRange(from, to);
		if (range.days() > MAX_RANGE_DAYS) {
			throw new BadRequestException(
				"Range cannot exceed " + MAX_RANGE_DAYS + " days"
			);
		}
		return range;
	}

	private Range toRange(LocalDate from, LocalDate to) {
		return new Range(
			from,
			to,
			from.atStartOfDay(),
			to.plusDays(1).atStartOfDay()
		);
	}

	private LocalDateTime firstBucket(Range range, Granularity unit) {
		return unit == Granularity.MONTH
			? range.from().withDayOfMonth(1).atStartOfDay()
			: range.start();
	}

	private LocalDateTime nextBucket(LocalDateTime bucket, Granularity unit) {
		return switch (unit) {
			case HOUR -> bucket.plusHours(1);
			case DAY -> bucket.plusDays(1);
			case MONTH -> bucket.plusMonths(1);
		};
	}

	private BigDecimal zero() {
		return BigDecimal.ZERO.setScale(2);
	}
}
