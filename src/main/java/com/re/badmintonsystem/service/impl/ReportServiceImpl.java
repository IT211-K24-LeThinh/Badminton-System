package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.response.BookingStatisticsResponse;
import com.re.badmintonsystem.dto.response.RevenueResponse;
import com.re.badmintonsystem.entity.Booking;
import com.re.badmintonsystem.entity.enums.BookingStatus;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.BookingRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getAdminRevenue(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = getBookingsInRange(startDate, endDate);

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        Map<String, BigDecimal> revenueByDay = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().toString(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)));

        Map<String, BigDecimal> revenueByMonth = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().getYear() + "-" + String.format("%02d", b.getBookingDate().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)));

        log.info("Generated admin revenue report: {} to {}", startDate, endDate);

        return RevenueResponse.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(bookings.size())
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .revenueByDay(revenueByDay)
                .revenueByMonth(revenueByMonth)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingStatisticsResponse getAdminBookingStatistics(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = getBookingsInRange(startDate, endDate);

        return buildStatistics(bookings, startDate, endDate, "admin");
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getManagerRevenue(Long managerId, LocalDate startDate, LocalDate endDate) {
        userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        List<Booking> bookings = bookingRepository.findByCourt_ManagerId(managerId)
                .stream()
                .filter(b -> (startDate == null || !b.getBookingDate().isBefore(startDate))
                        && (endDate == null || !b.getBookingDate().isAfter(endDate)))
                .toList();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        Map<String, BigDecimal> revenueByDay = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().toString(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)));

        Map<String, BigDecimal> revenueByMonth = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().getYear() + "-" + String.format("%02d", b.getBookingDate().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Booking::getTotalPrice, BigDecimal::add)));

        log.info("Generated manager revenue report for manager {}: {} to {}", managerId, startDate, endDate);

        return RevenueResponse.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(bookings.size())
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .revenueByDay(revenueByDay)
                .revenueByMonth(revenueByMonth)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingStatisticsResponse getManagerBookingStatistics(Long managerId, LocalDate startDate, LocalDate endDate) {
        userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        List<Booking> bookings = bookingRepository.findByCourt_ManagerId(managerId)
                .stream()
                .filter(b -> (startDate == null || !b.getBookingDate().isBefore(startDate))
                        && (endDate == null || !b.getBookingDate().isAfter(endDate)))
                .toList();

        return buildStatistics(bookings, startDate, endDate, "manager");
    }

    private List<Booking> getBookingsInRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return bookingRepository.findAll().stream()
                    .filter(b -> !b.getBookingDate().isBefore(startDate)
                            && !b.getBookingDate().isAfter(endDate))
                    .toList();
        }
        return bookingRepository.findAll();
    }

    private BookingStatisticsResponse buildStatistics(List<Booking> bookings,
                                                       LocalDate startDate, LocalDate endDate,
                                                       String role) {
        Map<String, Long> byStatus = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStatus().name(),
                        Collectors.counting()));

        Map<String, Long> byDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Long> byCourt = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCourt().getName(),
                        Collectors.counting()));

        Map<String, Long> byTimeSlot = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getTimeSlot().getLabel(),
                        Collectors.counting()));

        log.info("Generated {} booking statistics", role);

        return BookingStatisticsResponse.builder()
                .totalBookings(bookings.size())
                .bookingsByStatus(byStatus)
                .bookingsByDay(byDay)
                .bookingsByCourt(byCourt)
                .bookingsByTimeSlot(byTimeSlot)
                .build();
    }
}
