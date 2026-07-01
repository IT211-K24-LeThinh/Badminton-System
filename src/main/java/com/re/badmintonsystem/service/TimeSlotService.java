package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.TimeSlotRequest;
import com.re.badmintonsystem.entity.TimeSlot;

import java.util.List;

public interface TimeSlotService {

    List<TimeSlot> findAll();

    TimeSlot create(TimeSlotRequest request);

    TimeSlot update(Long id, TimeSlotRequest request);

    void toggleActive(Long id);

    void delete(Long id);
}
