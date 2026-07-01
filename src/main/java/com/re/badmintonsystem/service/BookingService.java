package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.BookingRequest;
import com.re.badmintonsystem.dto.response.BookingResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.enums.BookingStatus;

public interface BookingService {

    BookingResponse create(Long customerId, BookingRequest request);

    BookingResponse findById(Long id);

    PagedResponse<BookingResponse> findMyBookings(Long customerId, BookingStatus status, int page, int size);

    BookingResponse cancelMyBooking(Long bookingId, Long customerId, String reason);

    // Manager
    PagedResponse<BookingResponse> findByManager(Long managerId, BookingStatus status, int page, int size);

    BookingResponse approve(Long bookingId, Long managerId, String note);

    BookingResponse reject(Long bookingId, Long managerId, String reason);

    BookingResponse checkIn(Long bookingId, Long managerId);

    BookingResponse complete(Long bookingId, Long managerId);

    // Admin
    PagedResponse<BookingResponse> findAllBookings(BookingStatus status, int page, int size);
}
