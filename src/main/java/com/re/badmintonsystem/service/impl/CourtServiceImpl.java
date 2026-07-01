package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.CourtRequest;
import com.re.badmintonsystem.dto.request.CourtStatusRequest;
import com.re.badmintonsystem.dto.response.CourtAvailabilityResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.*;
import com.re.badmintonsystem.entity.enums.CourtStatus;
import com.re.badmintonsystem.entity.enums.BookingStatus;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ForbiddenException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.mapper.CourtMapper;
import com.re.badmintonsystem.repository.*;
import com.re.badmintonsystem.service.CourtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtServiceImpl implements CourtService {

    private static final Logger log = LoggerFactory.getLogger(CourtServiceImpl.class);

    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final CourtImageRepository courtImageRepository;
    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    public CourtServiceImpl(CourtRepository courtRepository,
                            UserRepository userRepository,
                            CourtImageRepository courtImageRepository,
                            BookingRepository bookingRepository,
                            TimeSlotRepository timeSlotRepository) {
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
        this.courtImageRepository = courtImageRepository;
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    @Transactional
    public CourtResponse create(CourtRequest request, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        if (request.getCourtCode() != null && courtRepository.existsByCourtCode(request.getCourtCode())) {
            throw new BadRequestException("Court code already exists");
        }

        Court court = new Court();
        court.setManager(manager);
        court.setName(request.getName());
        court.setCourtCode(request.getCourtCode());
        court.setDescription(request.getDescription());
        court.setBasePricePerHour(request.getBasePricePerHour());
        court.setStatus(CourtStatus.ACTIVE);

        court = courtRepository.save(court);
        log.info("Created court: {} by manager {}", court.getName(), managerId);

        return CourtMapper.toResponseWithImageCount(court, 0);
    }

    @Override
    @Transactional
    public CourtResponse update(Long id, CourtRequest request, Long managerId) {
        Court court = findAndVerifyManagerAccess(id, managerId);

        if (request.getCourtCode() != null
                && !request.getCourtCode().equals(court.getCourtCode())
                && courtRepository.existsByCourtCode(request.getCourtCode())) {
            throw new BadRequestException("Court code already exists");
        }

        // Only update fields that are explicitly provided (not null)
        if (request.getName() != null) {
            court.setName(request.getName());
        }
        if (request.getCourtCode() != null) {
            court.setCourtCode(request.getCourtCode());
        }
        if (request.getDescription() != null) {
            court.setDescription(request.getDescription());
        }
        if (request.getBasePricePerHour() != null) {
            court.setBasePricePerHour(request.getBasePricePerHour());
        }

        court = courtRepository.save(court);
        log.info("Updated court: {}", court.getName());

        List<CourtImage> images = courtImageRepository.findByCourtId(court.getId());
        return CourtMapper.toResponseWithImages(court, images);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CourtStatusRequest request, Long managerId) {
        Court court = findAndVerifyManagerAccess(id, managerId);
        court.setStatus(request.getStatus());
        courtRepository.save(court);
        log.info("Updated court status: {} -> {}", court.getName(), request.getStatus());
    }

    @Override
    @Transactional
    public void delete(Long id, Long managerId) {
        Court court = findAndVerifyManagerAccess(id, managerId);

        List<Booking> activeBookings = bookingRepository.findByCourtId(id).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED
                        && b.getStatus() != BookingStatus.COMPLETED)
                .toList();

        if (!activeBookings.isEmpty()) {
            throw new BadRequestException("Cannot delete court with active bookings");
        }

        courtRepository.delete(court);
        log.info("Deleted court: {}", court.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourtResponse> findAll(String search,
                                                 CourtStatus status, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Court> courtPage;

        if (search != null && !search.isBlank()) {
            courtPage = courtRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (status != null) {
            courtPage = courtRepository.findByStatus(status, pageable);
        } else if (minPrice != null && maxPrice != null) {
            courtPage = courtRepository.findByBasePricePerHourBetween(
                    BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice), pageable);
        } else {
            courtPage = courtRepository.findAll(pageable);
        }

        List<CourtResponse> content = courtPage.getContent().stream()
                .map(court -> {
                    int imageCount = courtImageRepository.findByCourtId(court.getId()).size();
                    return CourtMapper.toResponseWithImageCount(court, imageCount);
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(content, courtPage.getNumber(), courtPage.getSize(),
                courtPage.getTotalElements(), courtPage.getTotalPages(), courtPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public CourtResponse findById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", id));
        List<CourtImage> images = courtImageRepository.findByCourtId(court.getId());
        return CourtMapper.toResponseWithImages(court, images);
    }

    @Override
    @Transactional(readOnly = true)
    public CourtAvailabilityResponse getAvailability(Long id, LocalDate date) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", id));

        if (court.getStatus() != CourtStatus.ACTIVE) {
            return CourtAvailabilityResponse.builder()
                    .courtId(id)
                    .courtName(court.getName())
                    .date(date)
                    .slots(List.of())
                    .build();
        }

        List<TimeSlot> allSlots = timeSlotRepository.findAll();
        List<Booking> bookedSlots = bookingRepository.findByCourtIdAndBookingDate(id, date)
                .stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList();

        List<CourtAvailabilityResponse.TimeSlotAvailability> slots = allSlots.stream()
                .map(slot -> {
                    boolean isBooked = bookedSlots.stream()
                            .anyMatch(b -> b.getTimeSlot().getId().equals(slot.getId()));
                    return CourtAvailabilityResponse.TimeSlotAvailability.builder()
                            .timeSlotId(slot.getId())
                            .label(slot.getLabel())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .available(!isBooked)
                            .build();
                })
                .collect(Collectors.toList());

        return CourtAvailabilityResponse.builder()
                .courtId(id)
                .courtName(court.getName())
                .date(date)
                .slots(slots)
                .build();
    }

    private Court findAndVerifyManagerAccess(Long courtId, Long managerId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", courtId));

        if (!court.getManager().getId().equals(managerId)) {
            throw new ForbiddenException("You do not own this court");
        }

        return court;
    }
}
