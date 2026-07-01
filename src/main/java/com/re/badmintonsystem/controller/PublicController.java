package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.response.*;
import com.re.badmintonsystem.entity.TimeSlot;
import com.re.badmintonsystem.entity.enums.CourtStatus;
import com.re.badmintonsystem.service.CourtImageService;
import com.re.badmintonsystem.service.CourtService;
import com.re.badmintonsystem.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class PublicController {

    private final CourtService courtService;
    private final CourtImageService courtImageService;
    private final TimeSlotService timeSlotService;

    public PublicController(CourtService courtService,
                            CourtImageService courtImageService,
                            TimeSlotService timeSlotService) {
        this.courtService = courtService;
        this.courtImageService = courtImageService;
        this.timeSlotService = timeSlotService;
    }


    @GetMapping("/courts")
    public ResponseEntity<ApiResponse<PagedResponse<CourtResponse>>> getCourts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourtStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CourtResponse> response = courtService.findAll(search, status, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/courts/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> getCourt(@PathVariable Long id) {
        CourtResponse response = courtService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/courts/{id}/availability")
    public ResponseEntity<ApiResponse<CourtAvailabilityResponse>> getCourtAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CourtAvailabilityResponse response = courtService.getAvailability(id, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/courts/{courtId}/images")
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> getCourtImages(
            @PathVariable Long courtId) {
        List<CourtImageResponse> response = courtImageService.getImages(courtId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Time Slots (Public) ==========

    @GetMapping("/time-slots")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> getActiveTimeSlots() {
        List<TimeSlot> allSlots = timeSlotService.findAll();
        List<TimeSlot> activeSlots = allSlots.stream()
                .filter(TimeSlot::isActive)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(activeSlots));
    }
}
