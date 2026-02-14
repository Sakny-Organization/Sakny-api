package com.sakny.user.service;

import com.sakny.common.dto.*;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ProfileErrorCode;
import com.sakny.common.service.StorageService;
import com.sakny.user.entity.*;
import com.sakny.user.mapper.ProfileMapper;
import com.sakny.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final CityRepository cityRepository;
    private final ProfileMapper profileMapper;
    private final StorageService storageService;

    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileRequest request) {
        log.info("Creating profile for user ID: {}", userId);

        if (profileRepository.existsByUserId(userId)) {
            throw new BusinessException(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));

        validateBudgetRange(request);
        validateLocations(request);

        Governorate currentGov = resolveGovernorate(request.getCurrentGovernorateId());
        City currentCity = resolveCity(request.getCurrentCityId());

        UserProfile profile = profileMapper.toEntity(request, user, currentGov, currentCity);

        // Add preferred areas
        for (PreferredAreaRequest areaReq : request.getPreferredAreas()) {
            PreferredArea area = buildPreferredArea(areaReq);
            profile.addPreferredArea(area);
        }

        UserProfile saved = profileRepository.save(profile);
        log.info("Profile created successfully for user ID: {}", userId);

        return profileMapper.toResponse(saved);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // Validate budget range if both are provided or one is being updated
        validateBudgetRangeForUpdate(request, profile);

        // Validate locations if provided
        validateLocationsForUpdate(request);

        // Resolve locations (pass null if not provided to let IGNORE strategy skip)
        Governorate currentGov = request.getCurrentGovernorateId() != null
                ? resolveGovernorate(request.getCurrentGovernorateId())
                : null;
        City currentCity = request.getCurrentCityId() != null
                ? resolveCity(request.getCurrentCityId())
                : null;

        // Apply partial updates using mapper - null fields are ignored
        profileMapper.partialUpdateEntity(profile, request, currentGov, currentCity);

        // Handle preferred areas separately due to bidirectional relation
        if (request.getPreferredAreas() != null && !request.getPreferredAreas().isEmpty()) {
            profile.clearPreferredAreas();
            for (PreferredAreaRequest areaReq : request.getPreferredAreas()) {
                PreferredArea area = buildPreferredArea(areaReq);
                profile.addPreferredArea(area);
            }
        }

        UserProfile saved = profileRepository.save(profile);
        log.info("Profile updated successfully for user ID: {}", userId);

        return profileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        log.debug("Fetching profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        return profileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        log.debug("Fetching public profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        return profileMapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse uploadProfilePhoto(Long userId, MultipartFile file) {
        log.info("Uploading profile photo for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // Delete old photo if exists
        if (profile.getProfilePhotoUrl() != null && !profile.getProfilePhotoUrl().isBlank()) {
            String oldObjectKey = storageService.extractObjectKey(profile.getProfilePhotoUrl());
            if (oldObjectKey != null) {
                try {
                    storageService.deleteFile(oldObjectKey);
                    log.info("Deleted old profile photo for user ID: {}", userId);
                } catch (Exception e) {
                    log.warn("Failed to delete old profile photo for user {}: {}", userId, e.getMessage());
                }
            }
        }

        // Upload new photo
        String photoUrl = storageService.uploadProfilePhoto(file, userId);
        profile.setProfilePhotoUrl(photoUrl);

        UserProfile saved = profileRepository.save(profile);
        log.info("Profile photo uploaded successfully for user ID: {}", userId);

        return profileMapper.toResponse(saved);
    }

    @Transactional
    public ProfileResponse deleteProfilePhoto(Long userId) {
        log.info("Deleting profile photo for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        if (profile.getProfilePhotoUrl() != null && !profile.getProfilePhotoUrl().isBlank()) {
            String objectKey = storageService.extractObjectKey(profile.getProfilePhotoUrl());
            if (objectKey != null) {
                storageService.deleteFile(objectKey);
            }
            profile.setProfilePhotoUrl(null);
            profileRepository.save(profile);
            log.info("Profile photo deleted successfully for user ID: {}", userId);
        }

        return profileMapper.toResponse(profile);
    }

    // ===== Validation =====

    private void validateBudgetRange(ProfileRequest request) {
        if (request.getBudgetMin() != null && request.getBudgetMax() != null
                && request.getBudgetMin() > request.getBudgetMax()) {
            throw new BusinessException(ProfileErrorCode.INVALID_BUDGET_RANGE);
        }
    }

    private void validateBudgetRangeForUpdate(ProfileUpdateRequest request, UserProfile profile) {
        Integer newMin = request.getBudgetMin() != null ? request.getBudgetMin() : profile.getBudgetMin();
        Integer newMax = request.getBudgetMax() != null ? request.getBudgetMax() : profile.getBudgetMax();

        if (newMin != null && newMax != null && newMin > newMax) {
            throw new BusinessException(ProfileErrorCode.INVALID_BUDGET_RANGE);
        }
    }

    private void validateLocations(ProfileRequest request) {
        // Validate current location if provided
        if (request.getCurrentGovernorateId() != null) {
            if (!governorateRepository.existsById(request.getCurrentGovernorateId())) {
                throw new BusinessException(ProfileErrorCode.INVALID_GOVERNORATE);
            }
        }
        if (request.getCurrentCityId() != null) {
            City city = cityRepository.findById(request.getCurrentCityId())
                    .orElseThrow(() -> new BusinessException(ProfileErrorCode.INVALID_CITY));
            if (request.getCurrentGovernorateId() != null
                    && !city.getGovernorate().getId().equals(request.getCurrentGovernorateId())) {
                throw new BusinessException(ProfileErrorCode.INVALID_CITY);
            }
        }

        // Validate preferred areas
        if (request.getPreferredAreas() != null) {
            for (PreferredAreaRequest area : request.getPreferredAreas()) {
                if (!governorateRepository.existsById(area.getGovernorateId())) {
                    throw new BusinessException(ProfileErrorCode.INVALID_GOVERNORATE,
                            "Governorate with ID " + area.getGovernorateId() + " not found");
                }
                City city = cityRepository.findById(area.getCityId())
                        .orElseThrow(() -> new BusinessException(ProfileErrorCode.INVALID_CITY,
                                "City with ID " + area.getCityId() + " not found"));
                if (!city.getGovernorate().getId().equals(area.getGovernorateId())) {
                    throw new BusinessException(ProfileErrorCode.INVALID_CITY,
                            "City " + area.getCityId() + " does not belong to governorate " + area.getGovernorateId());
                }
            }
        }
    }

    // ===== Helpers =====

    private PreferredArea buildPreferredArea(PreferredAreaRequest areaReq) {
        Governorate gov = governorateRepository.findById(areaReq.getGovernorateId())
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.INVALID_GOVERNORATE));
        City city = cityRepository.findById(areaReq.getCityId())
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.INVALID_CITY));

        return PreferredArea.builder()
                .governorate(gov)
                .city(city)
                .street(areaReq.getStreet())
                .build();
    }

    private Governorate resolveGovernorate(Integer id) {
        if (id == null) return null;
        return governorateRepository.findById(id).orElse(null);
    }

    private City resolveCity(Integer id) {
        if (id == null) return null;
        return cityRepository.findById(id).orElse(null);
    }

    private void validateLocationsForUpdate(ProfileUpdateRequest request) {
        ProfileRequest pr = ProfileRequest.builder()
                        .age(request.getAge())
                        .bio(request.getBio())
                        .budgetMax(request.getBudgetMax())
                        .budgetMin(request.getBudgetMin())
                        .cleanliness(request.getCleanliness())
                        .companyName(request.getCompanyName())
                        .gender(request.getGender())
                        .instagram(request.getInstagram())
                        .linkedin(request.getLinkedin())
                        .occupation(request.getOccupation())
                        .pets(request.getPets())
                        .prefCleanliness(request.getPrefCleanliness())
                        .prefPets(request.getPrefPets())
                        .prefSleepSchedule(request.getPrefSleepSchedule())
                        .prefSmoking(request.getPrefSmoking())
                        .roommateType(request.getRoommateType())
                        .sleepSchedule(request.getSleepSchedule())
                        .smoking(request.getSmoking())
                        .universityOrSchool(request.getUniversityOrSchool())
                        .currentGovernorateId(request.getCurrentGovernorateId())
                        .currentCityId(request.getCurrentCityId())
                        .preferredAreas(request.getPreferredAreas())
                        .build();

        validateLocations(pr);
    }
}
