package com.billiards.store.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.billiards.store.repository.FinancialReportRepository;

@Service
public class DashboardService {
    private final FinancialReportRepository financialReportRepository;

    public DashboardService(FinancialReportRepository financialReportRepository) {
        this.financialReportRepository = financialReportRepository;
    }

    public BigDecimal getTotalRevenueByDate(LocalDate date) {
        return financialReportRepository.getTotalRevenueByDate(date);
    }

    public BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return financialReportRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalRevenueByYear(int year) {
        return financialReportRepository.getTotalRevenueByYear(year);
    }
}
