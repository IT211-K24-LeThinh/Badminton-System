package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.response.BookingStatisticsResponse;
import com.re.badmintonsystem.dto.response.RevenueResponse;

import java.time.LocalDate;

public interface ReportService {

    RevenueResponse getAdminRevenue(LocalDate startDate, LocalDate endDate);

    BookingStatisticsResponse getAdminBookingStatistics(LocalDate startDate, LocalDate endDate);

    RevenueResponse getManagerRevenue(Long managerId, LocalDate startDate, LocalDate endDate);

    BookingStatisticsResponse getManagerBookingStatistics(Long managerId, LocalDate startDate, LocalDate endDate);
}
