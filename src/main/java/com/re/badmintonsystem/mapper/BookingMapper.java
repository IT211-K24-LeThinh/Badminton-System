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
                .isPaid(booking.isPaid())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        if (booking.getConfirmedBy() != null) {
            builder.confirmedBy(booking.getConfirmedBy().getId())
                   .confirmedAt(booking.getConfirmedAt());
        }
        if (booking.getCancelledBy() != null) {
            builder.cancelledBy(booking.getCancelledBy().getId())
                   .cancelledAt(booking.getCancelledAt())
                   .cancellationReason(booking.getCancellationReason());
        }

        return builder.build();
    }
}
