package com.sakny.property.mapper;

import com.sakny.property.dto.AmenityResponse;
import com.sakny.property.dto.PropertyImageResponse;
import com.sakny.property.dto.PropertyRequest;
import com.sakny.property.dto.PropertyResponse;
import com.sakny.property.entity.Amenity;
import com.sakny.property.entity.Property;
import com.sakny.property.entity.PropertyImage;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "governorate", source = "governorate")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    Property toEntity(PropertyRequest request, User owner, Governorate governorate, City city, Set<Amenity> amenities);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "governorate", source = "governorate.nameEn")
    @Mapping(target = "city", source = "city.nameEn")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "images", source = "images")
    PropertyResponse toResponse(Property property);

    List<PropertyResponse> toResponseList(List<Property> properties);

    AmenityResponse toAmenityResponse(Amenity amenity);

    PropertyImageResponse toImageResponse(PropertyImage image);
}
