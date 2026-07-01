package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtResponse {

    private Long id;
    private String courtCode;
    private String name;
    private String description;
    private BigDecimal basePricePerHour;
    private String status;
    private Long managerId;
    private String managerName;
    private int imageCount;
    private List<CourtImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
