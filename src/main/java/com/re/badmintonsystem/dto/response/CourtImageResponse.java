package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtImageResponse {

    private Long id;
    private Long courtId;
    private String imageUrl;
    private String publicId;
    private Boolean isPrimary;
    private Integer displayOrder;
}
