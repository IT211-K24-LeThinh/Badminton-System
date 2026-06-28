package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    Optional<TimeSlot> findByStartTimeAndEndTime(java.time.LocalTime startTime, java.time.LocalTime endTime);
}
