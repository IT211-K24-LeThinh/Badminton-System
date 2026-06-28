package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.request.CourtComplexRequest;
import com.re.badmintonsystem.dto.request.CourtRequest;
import com.re.badmintonsystem.dto.request.CourtStatusRequest;
import com.re.badmintonsystem.dto.response.ApiResponse;
import com.re.badmintonsystem.dto.response.CourtComplexResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.security.CustomUserDetails;
import com.re.badmintonsystem.service.CourtComplexService;
import com.re.badmintonsystem.service.CourtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/manager")
public class ManagerController {

    private final CourtComplexService courtComplexService;
    private final CourtService courtService;

    public ManagerController(CourtComplexService courtComplexService,
                             CourtService courtService) {
        this.courtComplexService = courtComplexService;
        this.courtService = courtService;
    }

    // ========== Court Complexes ==========

    @PostMapping("/complexes")
    public ResponseEntity<ApiResponse<CourtComplexResponse>> createComplex(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourtComplexRequest request) {
        CourtComplexResponse response = courtComplexService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Court complex created successfully", response));
    }

    @PutMapping("/complexes/{id}")
    public ResponseEntity<ApiResponse<CourtComplexResponse>> updateComplex(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourtComplexRequest request) {
        CourtComplexResponse response = courtComplexService.update(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Court complex updated successfully", response));
    }

    @DeleteMapping("/complexes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComplex(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        courtComplexService.delete(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Court complex deleted successfully", null));
    }

    @GetMapping("/complexes")
    public ResponseEntity<ApiResponse<List<CourtComplexResponse>>> getMyComplexes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourtComplexResponse> response = courtComplexService.findMyComplexes(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
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
