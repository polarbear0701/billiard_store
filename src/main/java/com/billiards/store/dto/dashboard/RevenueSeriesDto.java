package com.billiards.store.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

import com.billiards.store.common.enumerated.Granularity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Revenue over time")
public class RevenueSeriesDto {
    @Schema(description = "First day requested", example = "2026-09-20")
    private LocalDate from;
    @Schema(description = "Last day requested", example = "2026-10-19")
    private LocalDate to;
    @Schema(description = "Bucket size used")
    private Granularity granularity;
    /** One point per bucket, including buckets with no sales (zeros). */
    @Schema(description = "One entry per bucket in time order, including buckets with no sales (zeros)")
    private List<RevenuePointDto> points;
}
