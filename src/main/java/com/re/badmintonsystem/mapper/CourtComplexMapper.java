package com.re.badmintonsystem.mapper;

import com.re.badmintonsystem.dto.response.CourtComplexResponse;
import com.re.badmintonsystem.entity.CourtComplex;

public class CourtComplexMapper {

    public static CourtComplexResponse toResponse(CourtComplex complex, int courtCount) {
        return CourtComplexResponse.builder()
                .id(complex.getId())
                .name(complex.getName())
                .address(complex.getAddress())
                .description(complex.getDescription())
                .status(complex.getStatus().name())
                .managerId(complex.getManager().getId())
                .managerName(complex.getManager().getFullName())
                .courtCount(courtCount)
                .createdAt(complex.getCreatedAt())
                .updatedAt(complex.getUpdatedAt())
                .build();
    }
}
