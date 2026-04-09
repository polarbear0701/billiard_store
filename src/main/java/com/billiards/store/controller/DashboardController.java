package com.billiards.store.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.service.DashboardService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for analytics and reporting")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/total-revenue/by-date")
    public ResponseEntity<SuccessResponse<?>> getTotalRevenueByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.OK.value(),
            "success",
            dashboardService.getTotalRevenueByDate(date),
            LocalDateTime.now()
        ));
    }

    @GetMapping("/total-revenue/by-date-range")
    public ResponseEntity<SuccessResponse<?>> getTotalRevenueByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.OK.value(),
            "success",
            dashboardService.getTotalRevenueByDateRange(startDate, endDate),
            LocalDateTime.now()
        ));
    }

    @GetMapping("/total-revenue/by-year")
    public ResponseEntity<SuccessResponse<?>> getTotalRevenueByYear(@RequestParam int year) {
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.OK.value(),
            "success",
            dashboardService.getTotalRevenueByYear(year),
            LocalDateTime.now()
        ));
    }
}
