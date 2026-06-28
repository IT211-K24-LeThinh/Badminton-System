package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.CourtComplexRequest;
import com.re.badmintonsystem.dto.response.CourtComplexResponse;
import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.Court;
import com.re.badmintonsystem.entity.CourtComplex;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ForbiddenException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.mapper.CourtComplexMapper;
import com.re.badmintonsystem.mapper.CourtMapper;
import com.re.badmintonsystem.repository.CourtComplexRepository;
import com.re.badmintonsystem.repository.CourtRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.service.CourtComplexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtComplexServiceImpl implements CourtComplexService {

    private static final Logger log = LoggerFactory.getLogger(CourtComplexServiceImpl.class);

    private final CourtComplexRepository complexRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    public CourtComplexServiceImpl(CourtComplexRepository complexRepository,
                                   CourtRepository courtRepository,
                                   UserRepository userRepository) {
        this.complexRepository = complexRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CourtComplexResponse create(Long managerId, CourtComplexRequest request) {
        if (complexRepository.existsByName(request.getName())) {
            throw new BadRequestException("Court complex with this name already exists");
        }

        CourtComplex complex = new CourtComplex();
        complex.setName(request.getName());
        complex.setAddress(request.getAddress());
        complex.setDescription(request.getDescription());
        complex.setManager(userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId)));
        complex.setStatus(CourtComplex.ComplexStatus.ACTIVE);

        complex = complexRepository.save(complex);
        log.info("Created court complex: {} by manager {}", complex.getName(), managerId);

        return CourtComplexMapper.toResponse(complex, 0);
    }

    @Override
    @Transactional
    public CourtComplexResponse update(Long complexId, Long managerId, CourtComplexRequest request) {
        CourtComplex complex = findAndVerifyOwnership(complexId, managerId);

        if (!complex.getName().equals(request.getName()) && complexRepository.existsByName(request.getName())) {
            throw new BadRequestException("Court complex with this name already exists");
        }

        complex.setName(request.getName());
        complex.setAddress(request.getAddress());
        complex.setDescription(request.getDescription());

        complex = complexRepository.save(complex);
        log.info("Updated court complex: {}", complex.getName());

        int courtCount = courtRepository.findByComplexId(complex.getId()).size();
        return CourtComplexMapper.toResponse(complex, courtCount);
    }

    @Override
    @Transactional
    public void delete(Long complexId, Long managerId) {
        CourtComplex complex = findAndVerifyOwnership(complexId, managerId);

        List<Court> courts = courtRepository.findByComplexId(complexId);
        if (!courts.isEmpty()) {
            throw new BadRequestException("Cannot delete complex with existing courts");
        }

        complexRepository.delete(complex);
        log.info("Deleted court complex: {}", complex.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtComplexResponse> findMyComplexes(Long managerId) {
        List<CourtComplex> complexes = complexRepository.findByManagerId(managerId);
        return complexes.stream()
                .map(c -> {
                    int courtCount = courtRepository.findByComplexId(c.getId()).size();
                    return CourtComplexMapper.toResponse(c, courtCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourtComplexResponse> findAll(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CourtComplex> complexPage;
        if (search != null && !search.isBlank()) {
            complexPage = complexRepository.findByStatusAndNameContainingIgnoreCase(
                    CourtComplex.ComplexStatus.ACTIVE, search, pageable);
        } else {
            complexPage = complexRepository.findByStatus(CourtComplex.ComplexStatus.ACTIVE, pageable);
        }

        List<CourtComplexResponse> content = complexPage.getContent().stream()
                .map(c -> {
                    int courtCount = courtRepository.findByComplexId(c.getId()).size();
                    return CourtComplexMapper.toResponse(c, courtCount);
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(content, complexPage.getNumber(), complexPage.getSize(),
                complexPage.getTotalElements(), complexPage.getTotalPages(), complexPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public CourtComplexResponse findById(Long id) {
        CourtComplex complex = complexRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourtComplex", "id", id));
        int courtCount = courtRepository.findByComplexId(id).size();
        return CourtComplexMapper.toResponse(complex, courtCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> findCourtsByComplexId(Long complexId) {
        List<Court> courts = courtRepository.findByComplexId(complexId);
        return courts.stream()
                .map(CourtMapper::toResponse)
                .collect(Collectors.toList());
    }

    private CourtComplex findAndVerifyOwnership(Long complexId, Long managerId) {
        CourtComplex complex = complexRepository.findById(complexId)
                .orElseThrow(() -> new ResourceNotFoundException("CourtComplex", "id", complexId));

        if (!complex.getManager().getId().equals(managerId)) {
            throw new ForbiddenException("You do not own this court complex");
        }

        return complex;
    }
}
