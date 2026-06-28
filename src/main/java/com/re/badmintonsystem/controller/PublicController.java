package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.response.*;
import com.re.badmintonsystem.entity.Court.CourtStatus;
import com.re.badmintonsystem.service.CourtComplexService;
import com.re.badmintonsystem.service.CourtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class PublicController {

    private final CourtComplexService courtComplexService;
    private final CourtService courtService;

    public PublicController(CourtComplexService courtComplexService,
                            CourtService courtService) {
        this.courtComplexService = courtComplexService;
        this.courtService = courtService;
    }

    // ========== Court Complexes (Public) ==========

    @GetMapping("/complexes")
    public ResponseEntity<ApiResponse<PagedResponse<CourtComplexResponse>>> getComplexes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CourtComplexResponse> response = courtComplexService.findAll(search, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/complexes/{id}")
    public ResponseEntity<ApiResponse<CourtComplexResponse>> getComplex(@PathVariable Long id) {
        CourtComplexResponse response = courtComplexService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/complexes/{id}/courts")
    public ResponseEntity<ApiResponse<List<CourtResponse>>> getCourtsByComplex(@PathVariable Long id) {
        List<CourtResponse> response = courtComplexService.findCourtsByComplexId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Courts (Public) ==========

    @GetMapping("/courts")
    public ResponseEntity<ApiResponse<PagedResponse<CourtResponse>>> getCourts(
            @RequestParam(required = false) Long complexId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourtStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CourtResponse> response = courtService.findAll(complexId, search, status, page, size);
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
