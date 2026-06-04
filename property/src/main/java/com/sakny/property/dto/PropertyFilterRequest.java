package com.sakny.property.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PropertyFilterRequest {
    private Integer governorateId;
    private Integer cityId;
    private String propertyType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean furnished;
}
