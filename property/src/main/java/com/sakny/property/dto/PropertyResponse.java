package com.sakny.property.dto;

import com.sakny.common.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String description;
    private BigDecimal price;
    private String propertyType;
    private String governorate;
    private Integer governorateId;
    private String city;
    private Integer cityId;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer roomsCount;
    private Integer bathroomsCount;
    private Integer floorNumber;
    private Boolean isFullyFurnished;
    private LocalDate availableFrom;
    private Set<AmenityResponse> amenities;
    private List<PropertyImageResponse> images;
    private LocalDateTime createdAt;

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
    private String status;

    private Gender prefTenantGender;
    private RoommateType prefTenantType;
    private SmokingPreference prefSmoking;
    private PetPreference prefPets;
    private SleepSchedulePreference prefSleepSchedule;
    private CleanlinessPreference prefCleanliness;
    private Integer prefMinAge;
    private Integer prefMaxAge;
}
