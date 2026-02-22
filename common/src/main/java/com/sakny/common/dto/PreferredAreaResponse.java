package com.sakny.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferredAreaResponse {
    private Long id;
    private LocationDto governorate;
    private LocationDto city;
    private String street;
}
