package com.sakny.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferredAreaRequest {

    @NotNull(message = "Governorate ID is required")
    private Integer governorateId;

    @NotNull(message = "City ID is required")
    private Integer cityId;

    private String street;
}
