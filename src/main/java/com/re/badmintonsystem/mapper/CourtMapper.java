package com.re.badmintonsystem.mapper;

import com.re.badmintonsystem.dto.response.CourtResponse;
import com.re.badmintonsystem.entity.Court;

public class CourtMapper {

    public static CourtResponse toResponse(Court court) {
        return CourtResponse.builder()
                .id(court.getId())
                .courtCode(court.getCourtCode())
                .name(court.getName())
                .description(court.getDescription())
                .basePricePerHour(court.getBasePricePerHour())
                .status(court.getStatus().name())
                .managerId(court.getManager().getId())
                .managerName(court.getManager().getFullName())
                .createdAt(court.getCreatedAt())
                .updatedAt(court.getUpdatedAt())
                .build();
    }

    public static CourtResponse toResponseWithImageCount(Court court, int imageCount) {
        return CourtResponse.builder()
                .id(court.getId())
                .courtCode(court.getCourtCode())
                .name(court.getName())
                .description(court.getDescription())
                .basePricePerHour(court.getBasePricePerHour())
                .status(court.getStatus().name())
                .managerId(court.getManager().getId())
                .managerName(court.getManager().getFullName())
                .imageCount(imageCount)
                .createdAt(court.getCreatedAt())
                .updatedAt(court.getUpdatedAt())
                .build();
    }
}
