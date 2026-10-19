package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RevenuePointDto {
    /** Start of the hour, day or month this point covers. */
    private LocalDateTime bucket;
    private BigDecimal revenue;
    private BigDecimal tableRevenue;
    private BigDecimal fnbRevenue;
    private long invoiceCount;
}
