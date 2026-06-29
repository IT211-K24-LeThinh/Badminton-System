package com.re.badmintonsystem.mapper;

import com.re.badmintonsystem.dto.response.BookingResponse;
import com.re.badmintonsystem.entity.Booking;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .timeSlotId(booking.getTimeSlot().getId())
                .timeSlotLabel(booking.getTimeSlot().getLabel())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .customerNote(booking.getCustomerNote())
                .managerNote(booking.getManagerNote())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        if (booking.getApprovedBy() != null) {
            builder.approvedBy(booking.getApprovedBy().getId())
                   .approvedAt(booking.getApprovedAt());
        }

        return builder.build();
    }
}
