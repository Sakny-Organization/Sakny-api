package com.sakny.listing.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakny.common.dto.*;
import com.sakny.common.model.Amenity;
import com.sakny.listing.entity.Listing;
import com.sakny.listing.entity.ListingImage;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ListingMapper {

    ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "governorate", source = "governorate")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "amenities", source = "request.amenities", qualifiedByName = "amenitiesToJson")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Listing toEntity(ListingRequest request, User user, Governorate governorate, City city);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", source = "user.name")
    @Mapping(target = "ownerProfilePhotoUrl", expression = "java(getOwnerProfilePhotoUrl(listing))")
    @Mapping(target = "ownerVerified", expression = "java(listing.getUser().isEnabled())")
    @Mapping(target = "governorate", source = "governorate", qualifiedByName = "toLocationDto")
    @Mapping(target = "city", source = "city", qualifiedByName = "toLocationDto")
    @Mapping(target = "amenities", source = "amenities", qualifiedByName = "jsonToAmenities")
    @Mapping(target = "imageUrls", expression = "java(getImageUrls(listing))")
    @Mapping(target = "availableSpots", expression = "java(listing.getAvailableSpots())")
    @Mapping(target = "matchPercentage", ignore = true)
    ListingResponse toResponse(Listing listing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "governorate", source = "governorate")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "amenities", source = "request.amenities", qualifiedByName = "amenitiesToJson")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void partialUpdateEntity(@MappingTarget Listing listing,
                             ListingUpdateRequest request,
                             Governorate governorate,
                             City city);

    @Named("toLocationDto")
    default LocationDto toLocationDto(Governorate governorate) {
        if (governorate == null) return null;
        return LocationDto.builder()
                .id(governorate.getId())
                .nameEn(governorate.getNameEn())
                .nameAr(governorate.getNameAr())
                .build();
    }

    @Named("toLocationDto")
    default LocationDto toLocationDto(City city) {
        if (city == null) return null;
        return LocationDto.builder()
                .id(city.getId())
                .nameEn(city.getNameEn())
                .nameAr(city.getNameAr())
                .build();
    }

    @Named("amenitiesToJson")
    default String amenitiesToJson(List<Amenity> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "[]";
        }
        try {
            return JSON_MAPPER.writeValueAsString(amenities);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Named("jsonToAmenities")
    default List<Amenity> jsonToAmenities(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return JSON_MAPPER.readValue(json, new TypeReference<List<Amenity>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    default List<String> getImageUrls(Listing listing) {
        if (listing.getImages() == null || listing.getImages().isEmpty()) {
            return Collections.emptyList();
        }
        return listing.getImages().stream()
                .sorted((a, b) -> {
                    // Primary image first, then by display order
                    if (Boolean.TRUE.equals(a.getIsPrimary())) return -1;
                    if (Boolean.TRUE.equals(b.getIsPrimary())) return 1;
                    return Integer.compare(
                            a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                            b.getDisplayOrder() != null ? b.getDisplayOrder() : 0
                    );
                })
                .map(ListingImage::getImageUrl)
                .collect(Collectors.toList());
    }

    default String getOwnerProfilePhotoUrl(Listing listing) {
        // This would ideally come from UserProfile, but we'd need to join
        // For now, return null - the service layer can enrich this
        return null;
    }
}

