package com.sakny.property.dto;

import com.sakny.common.model.*;
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

    private BigDecimal deposit;
    private Integer minimumStayMonths;
    private String paymentPeriod;
    private Integer maxOccupancy;
    private Integer parkingSpots;
    private Boolean utilitiesIncluded;
    private Boolean internetIncluded;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private String preferredTenant;

    private Gender prefTenantGender;
    private RoommateType prefTenantType;
    private SmokingPreference prefSmoking;
    private PetPreference prefPets;
    private SleepSchedulePreference prefSleepSchedule;
    private CleanlinessPreference prefCleanliness;
    private Integer prefMinAge;
    private Integer prefMaxAge;
}
