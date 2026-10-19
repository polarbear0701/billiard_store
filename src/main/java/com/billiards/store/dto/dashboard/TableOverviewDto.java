package com.billiards.store.dto.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.billiards.store.common.enumerated.TableStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableOverviewDto {
    private LocalDate from;
    private LocalDate to;
    /** Current number of tables per status, now (not for the period). */
    private Map<TableStatus, Long> statusCounts;
    private List<TableUtilizationDto> tables;
}
