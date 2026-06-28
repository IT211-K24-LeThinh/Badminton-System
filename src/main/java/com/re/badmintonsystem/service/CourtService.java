package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.CourtRequest;
import com.re.badmintonsystem.dto.request.CourtStatusRequest;
import com.re.badmintonsystem.dto.response.CourtAvailabilityResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.Court.CourtStatus;

import java.time.LocalDate;

public interface CourtService {

    CourtResponse create(CourtRequest request, Long managerId);

    CourtResponse update(Long id, CourtRequest request, Long managerId);

    void updateStatus(Long id, CourtStatusRequest request, Long managerId);

    void delete(Long id, Long managerId);

    PagedResponse<CourtResponse> findAll(Long complexId, String search, CourtStatus status, int page, int size);

    CourtResponse findById(Long id);

    CourtAvailabilityResponse getAvailability(Long id, LocalDate date);
}
