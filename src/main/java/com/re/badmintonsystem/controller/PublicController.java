package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.response.*;
import com.re.badmintonsystem.entity.enums.CourtStatus;
import com.re.badmintonsystem.service.CourtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1")
public class PublicController {

    private final CourtService courtService;

    public PublicController(CourtService courtService) {
        this.courtService = courtService;
    }

    // ========== Courts (Public) ==========

    @GetMapping("/courts")
    public ResponseEntity<ApiResponse<PagedResponse<CourtResponse>>> getCourts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourtStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CourtResponse> response = courtService.findAll(search, status, page, size);
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
}
