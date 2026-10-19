package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RevenueSummaryDto {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal revenue;
    private BigDecimal tableRevenue;
    private BigDecimal fnbRevenue;
    private long invoiceCount;
    private BigDecimal averageInvoice;
    private PreviousPeriod previous;
    /** Change versus the previous period in percent; null when the previous period had no revenue. */
    private BigDecimal revenueChangePercent;

    @Getter
    @AllArgsConstructor
    public static class PreviousPeriod {
        private LocalDate from;
        private LocalDate to;
        private BigDecimal revenue;
        private long invoiceCount;
    }
}
