package com.billiards.store.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.dto.dashboard.ChartDataDto;
import com.billiards.store.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for analytics and reporting")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/revenue/today")
    @Operation(summary = "Get hourly revenue for today")
    public ResponseEntity<List<ChartDataDto>> getRevenueToday() {
        return ResponseEntity.ok(dashboardService.getRevenueToday());
    }

    @GetMapping("/revenue/range")
    @Operation(summary = "Get daily revenue between two dates")
    public ResponseEntity<List<ChartDataDto>> getRevenueByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getRevenueByRange(startDate, endDate));
    }

    @GetMapping("/revenue/year")
    @Operation(summary = "Get monthly revenue for a specific year")
    public ResponseEntity<List<ChartDataDto>> getRevenueByYear(
            @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getRevenueByYear(year));
    }
}
