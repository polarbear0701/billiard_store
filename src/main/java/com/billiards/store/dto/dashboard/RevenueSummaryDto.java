package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Revenue totals for a period with comparison to the previous period")
public class RevenueSummaryDto {
    @Schema(description = "First day of the period (inclusive)", example = "2026-10-01")
    private LocalDate from;
    @Schema(description = "Last day of the period (inclusive)", example = "2026-10-19")
    private LocalDate to;
    @Schema(description = "Total of paid invoices in VND", example = "12500000.00")
    private BigDecimal revenue;
    @Schema(description = "Part of the revenue from table time, in VND", example = "9000000.00")
    private BigDecimal tableRevenue;
    @Schema(description = "Part of the revenue from food and drink, in VND", example = "3500000.00")
    private BigDecimal fnbRevenue;
    @Schema(description = "Number of paid invoices", example = "84")
    private long invoiceCount;
    @Schema(description = "Revenue divided by invoice count, 0 if there are no invoices", example = "148809.52")
    private BigDecimal averageInvoice;
    @Schema(description = "The period of the same length directly before this one")
    private PreviousPeriod previous;
    /** Change versus the previous period in percent; null when the previous period had no revenue. */
    @Schema(description = "Revenue change versus the previous period, in percent (negative = drop). null when the previous period had no revenue", example = "-12.5")
    private BigDecimal revenueChangePercent;

    @Getter
    @AllArgsConstructor
    @Schema(description = "Totals of the previous period")
    public static class PreviousPeriod {
        @Schema(description = "First day of the previous period", example = "2026-09-12")
        private LocalDate from;
        @Schema(description = "Last day of the previous period", example = "2026-09-30")
        private LocalDate to;
        @Schema(description = "Revenue of the previous period in VND", example = "14285714.00")
        private BigDecimal revenue;
        @Schema(description = "Paid invoices in the previous period", example = "90")
        private long invoiceCount;
    }
}
