package com.billiards.store.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

import com.billiards.store.common.enumerated.Granularity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RevenueSeriesDto {
    private LocalDate from;
    private LocalDate to;
    private Granularity granularity;
    /** One point per bucket, including buckets with no sales (zeros). */
    private List<RevenuePointDto> points;
}
