package com.re.badmintonsystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRolesRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<Long> roleIds;
}
