package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.request.CourtRequest;
import com.re.badmintonsystem.dto.request.CourtStatusRequest;
import com.re.badmintonsystem.dto.response.ApiResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.security.CustomUserDetails;
import com.re.badmintonsystem.service.CourtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/manager")
public class ManagerController {

    private final CourtService courtService;

    public ManagerController(CourtService courtService) {
        this.courtService = courtService;
    }

    // ========== Courts ==========

    @PostMapping("/courts")
    public ResponseEntity<ApiResponse<CourtResponse>> createCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.create(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Court created successfully", response));
    }

    @PutMapping("/courts/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> updateCourt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.update(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Court updated successfully", response));
    }

    @PatchMapping("/courts/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateCourtStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourtStatusRequest request) {
        courtService.updateStatus(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Court status updated successfully", null));
    }

    @DeleteMapping("/courts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        courtService.delete(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Court deleted successfully", null));
    }
}
