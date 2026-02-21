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
 * Request DTO for creating a new listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    private Integer rentAmount;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @Min(value = 1, message = "Total bedrooms must be at least 1")
    @Max(value = 20, message = "Total bedrooms must not exceed 20")
    private Integer totalBedrooms;

    @Min(value = 1, message = "Total roommates capacity must be at least 1")
    @Max(value = 20, message = "Total roommates must not exceed 20")
    private Integer totalRoommates;

    @Min(value = 0, message = "Current roommates cannot be negative")
    private Integer currentRoommates;

    @NotNull(message = "Preferred gender is required")
    private Gender preferredGender;

    @Min(value = 1, message = "Minimum stay must be at least 1 month")
    private Integer minimumStayMonths;

    @NotNull(message = "Available from date is required")
    @FutureOrPresent(message = "Available from date must be today or in the future")
    private LocalDate availableFrom;

    @NotNull(message = "Governorate is required")
    private Integer governorateId;

    @NotNull(message = "City is required")
    private Integer cityId;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private List<Amenity> amenities;

    @NotNull(message = "Bills included status is required")
    private Boolean billsIncluded;

    private Boolean petsAllowed;

    private Boolean smokingAllowed;
}

