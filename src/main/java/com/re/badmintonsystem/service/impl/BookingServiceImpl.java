package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.BookingRequest;
import com.re.badmintonsystem.dto.response.BookingResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.*;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.mapper.BookingMapper;
import com.re.badmintonsystem.repository.*;
import com.re.badmintonsystem.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              CourtRepository courtRepository,
                              TimeSlotRepository timeSlotRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingResponse create(Long customerId, BookingRequest request) {
        log.info("Creating booking: userId={}, courtId={}, date={}, slotId={}",
                customerId, request.getCourtId(), request.getBookingDate(), request.getTimeSlotId());

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", request.getCourtId()));

        if (court.getStatus() != Court.CourtStatus.ACTIVE) {
            throw new BadRequestException("Court is not available for booking");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getTimeSlotId()));

        // Check if current date is in the past (after creation validation)
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book for a past date");
        }

        // Check duplicate active booking
        boolean exists = bookingRepository.existsActiveBooking(
                request.getCourtId(), request.getBookingDate(), request.getTimeSlotId());
        if (exists) {
            throw new BadRequestException("This time slot is already booked");
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCourt(court);
        booking.setTimeSlot(timeSlot);
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setNotes(request.getNotes());
        booking.setTotalPrice(court.getBasePricePerHour());
        booking.setPaid(false);

        booking = bookingRepository.save(booking);
        log.info("Booking created: id={}", booking.getId());

        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> findMyBookings(Long customerId, Booking.BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Booking> bookingPage;
        if (status != null) {
            bookingPage = bookingRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else {
            bookingPage = bookingRepository.findByCustomerId(customerId, pageable);
        }

        List<BookingResponse> content = bookingPage.getContent().stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, bookingPage.getNumber(), bookingPage.getSize(),
                bookingPage.getTotalElements(), bookingPage.getTotalPages(), bookingPage.isLast());
    }

    @Override
    @Transactional
    public BookingResponse cancelMyBooking(Long bookingId, Long customerId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("This is not your booking");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING
                && booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Can only cancel bookings in PENDING or CONFIRMED status");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledBy(booking.getCustomer());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        booking = bookingRepository.save(booking);
        log.info("Booking cancelled: id={}, reason={}", booking.getId(), reason);

        return BookingMapper.toResponse(booking);
    }
}

