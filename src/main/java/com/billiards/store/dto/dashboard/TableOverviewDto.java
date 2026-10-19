package com.billiards.store.dto.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.billiards.store.common.enumerated.TableStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Table statuses and usage for a period")
public class TableOverviewDto {
    @Schema(description = "First day of the period", example = "2026-10-19")
    private LocalDate from;
    @Schema(description = "Last day of the period", example = "2026-10-19")
    private LocalDate to;
    /** Current number of tables per status, now (not for the period). */
    @Schema(description = "How many tables currently have each status (now, not for the period). Keys: AVAILABLE, OCCUPIED, MAINTENANCE")
    private Map<TableStatus, Long> statusCounts;
    @Schema(description = "One entry per table, ordered by table code")
    private List<TableUtilizationDto> tables;
}
