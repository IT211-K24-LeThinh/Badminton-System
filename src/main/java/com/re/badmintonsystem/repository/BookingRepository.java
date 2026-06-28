package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.Booking;
import com.re.badmintonsystem.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);
    Page<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status, Pageable pageable);
    List<Booking> findByCourtId(Long courtId);
    List<Booking> findByCourtIdAndBookingDate(Long courtId, LocalDate bookingDate);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByBookingDate(LocalDate bookingDate);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.court.id = :courtId " +
           "AND b.bookingDate = :date AND b.timeSlot.id = :timeSlotId " +
           "AND b.status NOT IN ('CANCELLED')")
    boolean existsActiveBooking(@Param("courtId") Long courtId,
                                @Param("date") LocalDate date,
                                @Param("timeSlotId") Long timeSlotId);

    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusNot(
            Long courtId, LocalDate bookingDate, Long timeSlotId, BookingStatus status);
}
