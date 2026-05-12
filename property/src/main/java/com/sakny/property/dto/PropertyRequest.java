package com.sakny.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Property type is required")
    private String propertyType;

    @NotNull(message = "Governorate is required")
    private Integer governorateId;

    @NotNull(message = "City is required")
    private Integer cityId;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private Integer roomsCount;
    private Integer bathroomsCount;
    private Integer floorNumber;
    private Boolean isFullyFurnished;
    private LocalDate availableFrom;

    private Set<Long> amenityIds;
}
