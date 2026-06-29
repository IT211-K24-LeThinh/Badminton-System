package com.re.badmintonsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtRequest {

    @NotBlank(message = "Court name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 50, message = "Court code must not exceed 50 characters")
    private String courtCode;

    private String description;

    @NotNull(message = "Base price per hour is required")
    @PositiveOrZero(message = "Base price must be zero or positive")
    private BigDecimal basePricePerHour;
}
