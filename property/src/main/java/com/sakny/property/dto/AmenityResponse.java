package com.sakny.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityResponse {
    private Long id;
    private String nameEn;
    private String nameAr;
    private String icon;
}
