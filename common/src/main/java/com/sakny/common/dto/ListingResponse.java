package com.sakny.common.dto;

import com.sakny.common.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for listing data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponse {

    private Long id;
    private Long userId;
    private String ownerName;
    private String ownerProfilePhotoUrl;
    private Boolean ownerVerified;

    private String title;
    private String description;
    private Integer rentAmount;
    private PropertyType propertyType;
    private RoomType roomType;
    private Integer totalBedrooms;
    private Integer totalRoommates;
    private Integer currentRoommates;
    private Integer availableSpots;

    private Gender preferredGender;
    private Integer minimumStayMonths;
    private LocalDate availableFrom;

    private LocationDto governorate;
    private LocationDto city;
    private String address;

    private List<Amenity> amenities;
    private Boolean billsIncluded;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;

    private List<String> imageUrls;
    private ListingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields for matching
    private Integer matchPercentage;
}

