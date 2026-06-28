package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.CourtComplexRequest;
import com.re.badmintonsystem.dto.response.CourtComplexResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;

import java.util.List;

public interface CourtComplexService {

    CourtComplexResponse create(Long managerId, CourtComplexRequest request);

    CourtComplexResponse update(Long complexId, Long managerId, CourtComplexRequest request);

    void delete(Long complexId, Long managerId);

    List<CourtComplexResponse> findMyComplexes(Long managerId);

    PagedResponse<CourtComplexResponse> findAll(String search, int page, int size);

    CourtComplexResponse findById(Long id);

    List<CourtResponse> findCourtsByComplexId(Long complexId);
}
