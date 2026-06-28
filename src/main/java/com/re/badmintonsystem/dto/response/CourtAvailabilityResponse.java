package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtAvailabilityResponse {

    private Long courtId;
    private String courtName;
    private LocalDate date;
    private List<TimeSlotAvailability> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlotAvailability {
        private Long timeSlotId;
        private String label;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
    }
}
