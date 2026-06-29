package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long courtId;
    private String courtName;
    private Long timeSlotId;
    private String timeSlotLabel;
    private LocalDate bookingDate;
    private String status;
    private BigDecimal totalPrice;
    private boolean isPaid;
    private String notes;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private Long cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
