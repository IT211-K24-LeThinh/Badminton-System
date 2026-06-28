package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.BookingRequest;
import com.re.badmintonsystem.dto.response.BookingResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.Booking.BookingStatus;

public interface BookingService {

    BookingResponse create(Long customerId, BookingRequest request);

    BookingResponse findById(Long id);

    PagedResponse<BookingResponse> findMyBookings(Long customerId, BookingStatus status, int page, int size);

    BookingResponse cancelMyBooking(Long bookingId, Long customerId, String reason);
}
