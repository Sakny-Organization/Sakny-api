package com.sakny.common.dto;

import com.sakny.common.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Criteria for searching listings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchCriteria {

    private Integer governorateId;
    private Integer cityId;
    private String location; // Free text search on location/address

    private Integer minRent;
    private Integer maxRent;

    private PropertyType propertyType;
    private RoomType roomType;

    private Gender preferredGender;

    private LocalDate availableFrom;
    private LocalDate availableTo;

    private List<Amenity> requiredAmenities;

    private Boolean billsIncluded;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;

    private Integer minBedrooms;
    private Integer maxBedrooms;

    private Boolean hasAvailableSpots;

    // Sorting
    private String sortBy; // "price", "date", "match"
    private String sortDirection; // "asc", "desc"

    // Pagination
    private Integer page;
    private Integer size;
}

