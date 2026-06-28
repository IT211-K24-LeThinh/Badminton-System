package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtComplexResponse {

    private Long id;
    private String name;
    private String address;
    private String description;
    private String status;
    private Long managerId;
    private String managerName;
    private int courtCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
