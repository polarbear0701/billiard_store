package com.billiards.store.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.billiards.store.dto.dashboard.ChartDataDto;
import com.billiards.store.repository.InvoiceRepository;

@Service
public class DashboardService {
    private final InvoiceRepository invoiceRepository;

    public DashboardService(
        InvoiceRepository invoiceRepository
    ) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<ChartDataDto> getRevenueToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return mapToDto(invoiceRepository.findHourlyRevenue(start, end));
    }

    public List<ChartDataDto> getRevenueByRange(LocalDate startDate, LocalDate endDate) {
        return mapToDto(invoiceRepository.findDailyRevenue(
            startDate.atStartOfDay(), 
            endDate.atTime(LocalTime.MAX)
        ));
    }

    public List<ChartDataDto> getRevenueByYear(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return mapToDto(invoiceRepository.findMonthlyRevenue(start, end));
    }

    private List<ChartDataDto> mapToDto(List<Object[]> results) {
        return results.stream()
            .map(row -> new ChartDataDto((String) row[0], (BigDecimal) row[1]))
            .toList();
    }
}
