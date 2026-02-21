package com.sakny.common.dto;

import com.sakny.common.model.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating an existing listing.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingUpdateRequest {

    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Positive(message = "Rent amount must be positive")
    private Integer rentAmount;

    private PropertyType propertyType;

    private RoomType roomType;

    @Min(value = 1, message = "Total bedrooms must be at least 1")
    @Max(value = 20, message = "Total bedrooms must not exceed 20")
    private Integer totalBedrooms;

    @Min(value = 1, message = "Total roommates capacity must be at least 1")
    @Max(value = 20, message = "Total roommates must not exceed 20")
    private Integer totalRoommates;

    @Min(value = 0, message = "Current roommates cannot be negative")
    private Integer currentRoommates;

    private Gender preferredGender;

    @Min(value = 1, message = "Minimum stay must be at least 1 month")
    private Integer minimumStayMonths;

    private LocalDate availableFrom;

    private Integer governorateId;

    private Integer cityId;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private List<Amenity> amenities;

    private Boolean billsIncluded;

    private Boolean petsAllowed;

    private Boolean smokingAllowed;

    private ListingStatus status;
}

