package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Revenue of one hour, day or month")
public class RevenuePointDto {
    /** Start of the hour, day or month this point covers. */
    @Schema(description = "Start of the hour, day or month this point covers (ISO-8601 date-time in the store's timezone)", example = "2026-10-19T00:00:00")
    private LocalDateTime bucket;
    @Schema(description = "Total of invoices paid in this bucket, VND", example = "450000.00")
    private BigDecimal revenue;
    @Schema(description = "Table fees in this bucket, VND", example = "300000.00")
    private BigDecimal tableRevenue;
    @Schema(description = "Food and drink in this bucket, VND", example = "150000.00")
    private BigDecimal fnbRevenue;
    @Schema(description = "Paid invoices in this bucket", example = "4")
    private long invoiceCount;
}
