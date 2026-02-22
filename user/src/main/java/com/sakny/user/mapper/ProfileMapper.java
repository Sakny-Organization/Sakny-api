package com.sakny.user.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakny.common.dto.ProfileRequest;
import com.sakny.common.dto.ProfileResponse;
import com.sakny.common.dto.ProfileUpdateRequest;
import com.sakny.common.model.*;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import com.sakny.user.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, PreferredAreaMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface ProfileMapper {

    // ===== Request → Entity (create) =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "profilePhotoUrl", ignore = true)
    @Mapping(target = "personalityTraits", source = "request.personalityTraits", qualifiedByName = "serializeTraits")
    @Mapping(target = "roommateGender", source = "request.gender")
    @Mapping(target = "currentGovernorate", source = "currentGovernorate")
    @Mapping(target = "currentCity", source = "currentCity")
    @Mapping(target = "isComplete", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "preferredAreas", ignore = true) // handled manually due to bidirectional relation
    @Mapping(target = "age", source = "request.age")
    @Mapping(target = "gender", source = "request.gender")
    @Mapping(target = "occupation", source = "request.occupation")
    @Mapping(target = "universityOrSchool", source = "request.universityOrSchool")
    @Mapping(target = "companyName", source = "request.companyName")
    @Mapping(target = "bio", source = "request.bio")
    @Mapping(target = "instagram", source = "request.instagram")
    @Mapping(target = "linkedin", source = "request.linkedin")
    @Mapping(target = "smoking", source = "request.smoking")
    @Mapping(target = "pets", source = "request.pets")
    @Mapping(target = "sleepSchedule", source = "request.sleepSchedule")
    @Mapping(target = "cleanliness", source = "request.cleanliness", defaultValue = "3")
    @Mapping(target = "budgetMin", source = "request.budgetMin")
    @Mapping(target = "budgetMax", source = "request.budgetMax")
    @Mapping(target = "roommateType", source = "request.roommateType")
    @Mapping(target = "prefSmoking", source = "request.prefSmoking")
    @Mapping(target = "prefPets", source = "request.prefPets")
    @Mapping(target = "prefSleepSchedule", source = "request.prefSleepSchedule")
    @Mapping(target = "prefCleanliness", source = "request.prefCleanliness")
    @Mapping(target = "additionalNotes", source = "request.additionalNotes")
    UserProfile toEntity(ProfileRequest request, User user,
                         Governorate currentGovernorate, City currentCity);

    // ===== Partial Update (null fields are ignored) =====

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profilePhotoUrl", ignore = true)
    @Mapping(target = "personalityTraits", source = "request.personalityTraits", qualifiedByName = "serializeTraitsIgnoreNull")
    @Mapping(target = "roommateGender", ignore = true) // roommateGender follows gender in create, but separate in update
    @Mapping(target = "currentGovernorate", source = "currentGovernorate")
    @Mapping(target = "currentCity", source = "currentCity")
    @Mapping(target = "isComplete", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "preferredAreas", ignore = true) // handled manually due to bidirectional relation
    void partialUpdateEntity(@MappingTarget UserProfile profile, ProfileUpdateRequest request,
                             Governorate currentGovernorate, City currentCity);

    // ===== Entity → Response =====

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "gender", expression = "java(mapEnum(profile.getGender()))")
    @Mapping(target = "currentGovernorate", source = "currentGovernorate")
    @Mapping(target = "currentCity", source = "currentCity")
    @Mapping(target = "personalityTraits", source = "personalityTraits", qualifiedByName = "deserializeTraits")
    @Mapping(target = "smoking", expression = "java(mapEnum(profile.getSmoking()))")
    @Mapping(target = "pets", expression = "java(mapEnum(profile.getPets()))")
    @Mapping(target = "sleepSchedule", expression = "java(mapEnum(profile.getSleepSchedule()))")
    @Mapping(target = "roommateGender", expression = "java(mapEnum(profile.getRoommateGender()))")
    @Mapping(target = "roommateType", expression = "java(mapEnum(profile.getRoommateType()))")
    @Mapping(target = "prefSmoking", expression = "java(mapEnum(profile.getPrefSmoking()))")
    @Mapping(target = "prefPets", expression = "java(mapEnum(profile.getPrefPets()))")
    @Mapping(target = "prefSleepSchedule", expression = "java(mapEnum(profile.getPrefSleepSchedule()))")
    @Mapping(target = "prefCleanliness", expression = "java(mapEnum(profile.getPrefCleanliness()))")
    ProfileResponse toResponse(UserProfile profile);

    // ===== Custom mapping methods =====

    @Named("serializeTraits")
    default String serializeTraits(List<String> traits) {
        if (traits == null) return "[]";
        try {
            return new ObjectMapper().writeValueAsString(traits);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize personality traits", e);
        }
    }

    @Named("serializeTraitsIgnoreNull")
    default String serializeTraitsIgnoreNull(List<String> traits) {
        if (traits == null || traits.isEmpty()) return null; // Return null so IGNORE strategy skips it
        try {
            return new ObjectMapper().writeValueAsString(traits);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize personality traits", e);
        }
    }

    @Named("deserializeTraits")
    default List<String> deserializeTraits(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize personality traits", e);
        }
    }

    default String mapEnum(Enum<?> value) {
        return value != null ? value.name() : null;
    }
}
