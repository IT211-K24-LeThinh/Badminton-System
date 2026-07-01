package com.re.badmintonsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse {

    private BigDecimal totalRevenue;
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private Map<String, BigDecimal> revenueByDay;     // "2026-06-30" -> 500000
    private Map<String, BigDecimal> revenueByMonth;   // "2026-06" -> 15000000
}
