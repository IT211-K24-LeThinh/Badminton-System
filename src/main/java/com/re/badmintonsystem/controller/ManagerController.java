package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.request.*;
import com.re.badmintonsystem.dto.response.*;
import com.re.badmintonsystem.entity.enums.BookingStatus;
import com.re.badmintonsystem.security.CustomUserDetails;
import com.re.badmintonsystem.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/manager")
public class ManagerController {

    private final CourtService courtService;
    private final CourtImageService courtImageService;
    private final BookingService bookingService;
    private final ReportService reportService;

    public ManagerController(CourtService courtService,
                             CourtImageService courtImageService,
                             BookingService bookingService,
                             ReportService reportService) {
        this.courtService = courtService;
        this.courtImageService = courtImageService;
        this.bookingService = bookingService;
        this.reportService = reportService;
    }


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


    @PostMapping("/courts/{courtId}/images")
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> uploadCourtImages(
            @PathVariable Long courtId,
            @RequestParam("files") List<MultipartFile> files) {
        List<CourtImageResponse> response = courtImageService.uploadImages(courtId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Images uploaded successfully", response));
    }

    @DeleteMapping("/courts/{courtId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourtImage(
            @PathVariable Long courtId,
            @PathVariable Long imageId) {
        courtImageService.deleteImage(courtId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    @PatchMapping("/courts/{courtId}/images/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderCourtImages(
            @PathVariable Long courtId,
            @Valid @RequestBody ReorderRequest request) {
        courtImageService.reorderImages(courtId, request.getImageIds());
        return ResponseEntity.ok(ApiResponse.success("Images reordered successfully", null));
    }


    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getManagerBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<BookingResponse> response = bookingService.findByManager(
                userDetails.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/bookings/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ManagerActionRequest request) {
        BookingResponse response = bookingService.approve(id, userDetails.getId(), request.getNote());
        return ResponseEntity.ok(ApiResponse.success("Booking approved successfully", response));
    }

    @PatchMapping("/bookings/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ManagerActionRequest request) {
        BookingResponse response = bookingService.reject(id, userDetails.getId(), request.getNote());
        return ResponseEntity.ok(ApiResponse.success("Booking rejected", response));
    }

    @PatchMapping("/bookings/{id}/check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> checkInBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BookingResponse response = bookingService.checkIn(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking checked in successfully", response));
    }

    @PatchMapping("/bookings/{id}/complete")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BookingResponse response = bookingService.complete(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking completed successfully", response));
    }


    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenueReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RevenueResponse response = reportService.getManagerRevenue(userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/booking-statistics")
    public ResponseEntity<ApiResponse<BookingStatisticsResponse>> getBookingStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BookingStatisticsResponse response = reportService.getManagerBookingStatistics(userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
