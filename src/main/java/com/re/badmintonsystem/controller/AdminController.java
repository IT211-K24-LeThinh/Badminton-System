package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.request.*;
import com.re.badmintonsystem.dto.response.*;
import com.re.badmintonsystem.entity.AuditLog;
import com.re.badmintonsystem.entity.Role;
import com.re.badmintonsystem.entity.TimeSlot;
import com.re.badmintonsystem.entity.enums.BookingStatus;
import com.re.badmintonsystem.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final TimeSlotService timeSlotService;
    private final BookingService bookingService;
    private final AuditLogService auditLogService;
    private final ReportService reportService;

    public AdminController(UserService userService,
                           RoleService roleService,
                           TimeSlotService timeSlotService,
                           BookingService bookingService,
                           AuditLogService auditLogService,
                           ReportService reportService) {
        this.userService = userService;
        this.roleService = roleService;
        this.timeSlotService = timeSlotService;
        this.bookingService = bookingService;
        this.auditLogService = auditLogService;
        this.reportService = reportService;
    }

    // ========== User Management ==========

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<UserResponse> response = userService.findAll(search, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        UserResponse response = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", null));
    }

    @PatchMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRolesRequest request) {
        UserResponse response = userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", response));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // ========== Role Management ==========

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        List<Role> response = roleService.findAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<Role>> createRole(
            @Valid @RequestBody RoleRequest request) {
        Role response = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        Role response = roleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    // ========== Time Slots ==========

    @GetMapping("/time-slots")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> getTimeSlots() {
        List<TimeSlot> response = timeSlotService.findAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/time-slots")
    public ResponseEntity<ApiResponse<TimeSlot>> createTimeSlot(
            @Valid @RequestBody TimeSlotRequest request) {
        TimeSlot response = timeSlotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time slot created successfully", response));
    }

    @PutMapping("/time-slots/{id}")
    public ResponseEntity<ApiResponse<TimeSlot>> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotRequest request) {
        TimeSlot response = timeSlotService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Time slot updated successfully", response));
    }

    @PatchMapping("/time-slots/{id}/active")
    public ResponseEntity<ApiResponse<Void>> toggleTimeSlot(@PathVariable Long id) {
        timeSlotService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Time slot toggled successfully", null));
    }

    @DeleteMapping("/time-slots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Time slot deleted successfully", null));
    }

    // ========== Bookings (Admin) ==========

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<BookingResponse> response = bookingService.findAllBookings(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Audit Logs ==========

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<AuditLog> response = auditLogService.findAll(entityType, action, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<ApiResponse<AuditLog>> getAuditLog(@PathVariable Long id) {
        AuditLog response = auditLogService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Reports ==========

    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RevenueResponse response = reportService.getAdminRevenue(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/booking-statistics")
    public ResponseEntity<ApiResponse<BookingStatisticsResponse>> getBookingStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BookingStatisticsResponse response = reportService.getAdminBookingStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
