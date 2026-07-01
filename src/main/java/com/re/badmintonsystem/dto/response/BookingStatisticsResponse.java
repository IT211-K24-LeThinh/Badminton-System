package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatisticsResponse {

    private long totalBookings;
    private Map<String, Long> bookingsByStatus;        // "PENDING" -> 5, "CONFIRMED" -> 10, ...
    private Map<String, Long> bookingsByDay;            // "2026-06-30" -> 8
    private Map<String, Long> bookingsByCourt;          // "Court A" -> 12
    private Map<String, Long> bookingsByTimeSlot;       // "05:00-06:00" -> 6
}
