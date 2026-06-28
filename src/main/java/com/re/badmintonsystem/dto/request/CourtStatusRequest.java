package com.re.badmintonsystem.dto.request;

import com.re.badmintonsystem.entity.Court.CourtStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtStatusRequest {

    @NotNull(message = "Court status is required")
    private CourtStatus status;
}
