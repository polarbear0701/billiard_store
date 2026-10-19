package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Usage and revenue of one table")
public class TableUtilizationDto {
    @Schema(description = "Table code", example = "TBL000001")
    private String tableCode;
    @Schema(description = "Kind of table")
    private TableType tableType;
    @Schema(description = "Status of the table right now")
    private TableStatus currentStatus;
    @Schema(description = "Sessions played on this table in the period", example = "6")
    private long sessionCount;
    @Schema(description = "Minutes the table was in use in the period", example = "540")
    private long playedMinutes;
    /** Played time as a percentage of the period's opening hours, capped at 100. */
    @Schema(description = "Played minutes as a percentage of the period's opening hours, at most 100", example = "37.5")
    private BigDecimal utilizationPercent;
    @Schema(description = "Table fees from invoices paid in the period, VND", example = "540000.00")
    private BigDecimal tableRevenue;
}
