package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableUtilizationDto {
    private String tableCode;
    private TableType tableType;
    private TableStatus currentStatus;
    private long sessionCount;
    private long playedMinutes;
    /** Played time as a percentage of the period's opening hours, capped at 100. */
    private BigDecimal utilizationPercent;
    private BigDecimal tableRevenue;
}
