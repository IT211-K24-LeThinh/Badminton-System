package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.TimeSlotRequest;
import com.re.badmintonsystem.entity.TimeSlot;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ConflictException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.TimeSlotRepository;
import com.re.badmintonsystem.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotServiceImpl.class);

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> findAll() {
        return timeSlotRepository.findAll();
    }

    @Override
    @Transactional
    public TimeSlot create(TimeSlotRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (timeSlotRepository.existsByStartTimeAndEndTime(request.getStartTime(), request.getEndTime())) {
            throw new ConflictException("A time slot with this start and end time already exists");
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setLabel(request.getLabel());
        timeSlot.setActive(request.getActive() != null ? request.getActive() : true);

        timeSlot = timeSlotRepository.save(timeSlot);
        log.info("Time slot created: {}", timeSlot.getLabel());
        return timeSlot;
    }

    @Override
    @Transactional
    public TimeSlot update(Long id, TimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Check duplicate time range (excluding current slot)
        if (timeSlotRepository.existsByStartTimeAndEndTime(request.getStartTime(), request.getEndTime())
                && (!timeSlot.getStartTime().equals(request.getStartTime())
                    || !timeSlot.getEndTime().equals(request.getEndTime()))) {
            throw new ConflictException("A time slot with this start and end time already exists");
        }

        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setLabel(request.getLabel());

        timeSlot = timeSlotRepository.save(timeSlot);
        log.info("Time slot updated: {}", timeSlot.getLabel());
        return timeSlot;
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));

        timeSlot.setActive(!timeSlot.isActive());
        timeSlotRepository.save(timeSlot);
        log.info("Time slot {} toggled to active={}", timeSlot.getLabel(), timeSlot.isActive());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));

        timeSlotRepository.delete(timeSlot);
        log.info("Time slot deleted: {}", timeSlot.getLabel());
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }
    }
}
