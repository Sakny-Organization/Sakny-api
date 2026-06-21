package com.sakny.user.service;

import com.sakny.common.dto.*;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ProfileErrorCode;
import com.sakny.common.model.*;
import com.sakny.common.service.StorageService;
import com.sakny.user.service.MatchingService.MatchScoreResult;
import com.sakny.user.entity.*;
import com.sakny.user.mapper.ProfileMapper;
import com.sakny.user.repository.*;
import jakarta.persistence.criteria.Predicate;
import com.sakny.user.event.ProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final CityRepository cityRepository;
    private final SavedProfileRepository savedProfileRepository;
    private final ProfileMapper profileMapper;
    private final StorageService storageService;
    private final SafetyService safetyService;
    private final MatchingService matchingService;
    private final MatchExplanationService matchExplanationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileRequest request, MultipartFile profileImage) {
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
        uploadProfileImage(user.getId(), profileImage); // Handle photo upload after saving to get profile ID for storage path
        log.info("Profile created successfully for user ID: {}", userId);

        // Re-fetch with photo URL set before computing completion
        UserProfile refreshed = profileRepository.findByUserIdWithDetails(userId).orElse(saved);
        updateIsCompleteFlag(refreshed);
        eventPublisher.publishEvent(new ProfileUpdatedEvent(userId));
        return toEnrichedResponse(refreshed);
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

        UserProfile refreshed = profileRepository.findByUserIdWithDetails(userId).orElse(saved);
        updateIsCompleteFlag(refreshed);
        eventPublisher.publishEvent(new ProfileUpdatedEvent(userId));
        return toEnrichedResponse(refreshed);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        log.debug("Fetching profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        return toEnrichedResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        log.debug("Fetching public profile for user ID: {}", userId);

        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        return toEnrichedResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfileImage(Long userId, MultipartFile file) {
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

        return toEnrichedResponse(saved);
    }

    // upload profile image when creating a new profile for a user who doesn't have a profile yet(e.g., after registration)
    public void uploadProfileImage(Long userId, MultipartFile profileImage){
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        if (profileImage != null && !profileImage.isEmpty()) {
            // Upload new photo
            String photoUrl = storageService.uploadProfilePhoto(profileImage, userId);
            profile.setProfilePhotoUrl(photoUrl);

            UserProfile saved = profileRepository.save(profile);
            log.info("Profile photo uploaded successfully for user ID: {}", userId);
        }
    }

    @Transactional
    public ProfileResponse deleteProfileImage(Long userId) {
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

        return toEnrichedResponse(profile);
    }

    @Transactional(readOnly = true)
    public ContactInfoResponse getContactInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));
        return ContactInfoResponse.builder()
                .email(user.getEmail())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProfileResponse> getRoommates(Long currentUserId, RoommateFilterRequest filter, Pageable pageable) {
        UserProfile currentProfile = profileRepository.findByUserIdWithDetails(currentUserId).orElse(null);

        Specification<UserProfile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isComplete")));
            predicates.add(cb.notEqual(root.get("user").get("id"), currentUserId));

            // Strict same-gender filter
            if (currentProfile != null && currentProfile.getGender() != null) {
                predicates.add(cb.equal(root.get("gender"), currentProfile.getGender()));
            }

            // Exclude blocked users (both directions)
            Set<Long> excludedIds = safetyService.getExcludedUserIds(currentUserId);
            if (!excludedIds.isEmpty()) {
                predicates.add(cb.not(root.get("user").get("id").in(excludedIds)));
            }

            if (filter.getGender() != null) {
                predicates.add(cb.equal(root.get("gender"), filter.getGender()));
            }
            if (filter.getMinBudget() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budgetMax"), filter.getMinBudget()));
            }
            if (filter.getMaxBudget() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budgetMin"), filter.getMaxBudget()));
            }
            if (filter.getSmoking() != null) {
                predicates.add(cb.equal(root.get("smoking"), filter.getSmoking()));
            }
            if (filter.getPets() != null) {
                predicates.add(cb.equal(root.get("pets"), filter.getPets()));
            }
            if (filter.getSleepSchedule() != null) {
                predicates.add(cb.equal(root.get("sleepSchedule"), filter.getSleepSchedule()));
            }
            if (filter.getRoommateType() != null) {
                predicates.add(cb.equal(root.get("roommateType"), filter.getRoommateType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return profileRepository.findAll(spec, pageable).map(profileMapper::toResponse);
    }

    // ===== Compatibility & Match Scores =====

    @Transactional(readOnly = true)
    public MatchScoreResponse getCompatibility(Long currentUserId, Long targetUserId) {
        UserProfile seekerProfile = profileRepository.findByUserIdWithDetails(currentUserId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));
        UserProfile candidateProfile = profileRepository.findByUserIdWithDetails(targetUserId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));

        MatchScoreResult result = matchingService.computeMatchScore(seekerProfile, candidateProfile);

        String candidateName = candidateProfile.getUser().getName();
        String explanation = matchExplanationService.generateExplanation(result.score(), result.breakdown(), candidateName);
        List<String> discussionTopics = matchExplanationService.generateDiscussionTopics(result.breakdown());

        return MatchScoreResponse.builder()
                .userId(targetUserId)
                .score(result.score())
                .breakdown(result.breakdown())
                .strengths(result.strengths())
                .conflicts(result.conflicts())
                .explanation(explanation)
                .discussionTopics(discussionTopics)
                .profile(toEnrichedResponse(candidateProfile))
                .build();
    }

    @Transactional(readOnly = true)
    public List<MatchScoreResponse> getRoommatesWithScores(Long currentUserId, RoommateFilterRequest filter, Pageable pageable) {
        Page<ProfileResponse> page = getRoommates(currentUserId, filter, pageable);

        UserProfile seekerProfile = profileRepository.findByUserIdWithDetails(currentUserId).orElse(null);
        if (seekerProfile == null) {
            // No profile yet, return results without scores
            return page.getContent().stream()
                    .map(pr -> MatchScoreResponse.builder()
                            .userId(pr.getUserId())
                            .score(0.0)
                            .breakdown(Map.of())
                            .strengths(List.of())
                            .conflicts(List.of())
                            .profile(pr)
                            .build())
                    .collect(Collectors.toList());
        }

        List<MatchScoreResponse> scored = page.getContent().stream()
                .map(pr -> {
                    UserProfile candidateProfile = profileRepository.findByUserIdWithDetails(pr.getUserId()).orElse(null);
                    if (candidateProfile == null) {
                        return MatchScoreResponse.builder()
                                .userId(pr.getUserId())
                                .score(0.0)
                                .breakdown(Map.of())
                                .strengths(List.of())
                                .conflicts(List.of())
                                .explanation("")
                                .discussionTopics(List.of())
                                .profile(pr)
                                .build();
                    }
                    MatchScoreResult result = matchingService.computeMatchScore(seekerProfile, candidateProfile);
                    String candidateName = candidateProfile.getUser().getName();
                    String explanation = matchExplanationService.generateExplanation(result.score(), result.breakdown(), candidateName);
                    List<String> discussionTopics = matchExplanationService.generateDiscussionTopics(result.breakdown());
                    return MatchScoreResponse.builder()
                            .userId(pr.getUserId())
                            .score(result.score())
                            .breakdown(result.breakdown())
                            .strengths(result.strengths())
                            .conflicts(result.conflicts())
                            .explanation(explanation)
                            .discussionTopics(discussionTopics)
                            .profile(pr)
                            .build();
                })
                .sorted(Comparator.comparingDouble(MatchScoreResponse::getScore).reversed())
                .collect(Collectors.toList());

        return scored;
    }

    // ===== Saved Profiles =====

    @Transactional
    public void saveProfile(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ProfileErrorCode.USER_NOT_FOUND, "Cannot save your own profile");
        }
        if (!savedProfileRepository.existsByUser_IdAndSavedUser_Id(currentUserId, targetUserId)) {
            User current = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));
            User target = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));
            savedProfileRepository.save(SavedProfile.builder().user(current).savedUser(target).build());
        }
    }

    @Transactional
    public void unsaveProfile(Long currentUserId, Long targetUserId) {
        savedProfileRepository.deleteByUser_IdAndSavedUser_Id(currentUserId, targetUserId);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ProfileResponse> getSavedProfiles(Long currentUserId) {
        List<Long> savedIds = savedProfileRepository.findSavedUserIdsByUserId(currentUserId);
        return savedIds.stream()
                .map(id -> profileRepository.findByUserIdWithDetails(id).orElse(null))
                .filter(p -> p != null)
                .map(profileMapper::toResponse)
                .toList();
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

    // ===== Profile completion =====

    private void updateIsCompleteFlag(UserProfile profile) {
        Map<String, Boolean> steps = completionSteps(profile);
        boolean complete = steps.values().stream().allMatch(Boolean::booleanValue);
        if (complete != Boolean.TRUE.equals(profile.getIsComplete())) {
            profile.setIsComplete(complete);
            profileRepository.save(profile);
        }
    }

    /**
     * Ordered map of step label → whether it is complete.
     * Each entry is worth an equal share of 100%.
     */
    private Map<String, Boolean> completionSteps(UserProfile p) {
        Map<String, Boolean> steps = new LinkedHashMap<>();
        steps.put("Basic info (age & gender)", p.getAge() != null && p.getGender() != null);
        steps.put("Lifestyle (smoking, pets & sleep schedule)",
                p.getSmoking() != null && p.getPets() != null && p.getSleepSchedule() != null);
        steps.put("Personality traits",
                p.getPersonalityTraits() != null && !p.getPersonalityTraits().equals("[]") && !p.getPersonalityTraits().isBlank());
        steps.put("Budget range", p.getBudgetMin() != null && p.getBudgetMax() != null);
        steps.put("At least one preferred location",
                p.getPreferredAreas() != null && !p.getPreferredAreas().isEmpty());
        steps.put("Roommate preferences", p.getRoommateType() != null);
        return steps;
    }

    private Map<String, Boolean> allStepsIncludingOptional(UserProfile p) {
        Map<String, Boolean> steps = new LinkedHashMap<>(completionSteps(p));
        steps.put("Profile photo", p.getProfilePhotoUrl() != null && !p.getProfilePhotoUrl().isBlank());
        steps.put("Contact verified",
                Boolean.TRUE.equals(p.getUser().getIsEmailVerified()) ||
                Boolean.TRUE.equals(p.getUser().getIsPhoneVerified()));
        steps.put("ID Verification", Boolean.TRUE.equals(p.getUser().getIsVerified()));
        return steps;
    }

    private ProfileResponse toEnrichedResponse(UserProfile profile) {
        ProfileResponse response = profileMapper.toResponse(profile);

        Map<String, Boolean> requiredSteps = completionSteps(profile);
        Map<String, Boolean> allSteps = allStepsIncludingOptional(profile);

        List<String> missing = allSteps.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .toList();

        long completed = allSteps.values().stream().filter(Boolean::booleanValue).count();
        int percentage = (int) Math.round((completed * 100.0) / allSteps.size());

        boolean isComplete = requiredSteps.values().stream().allMatch(Boolean::booleanValue);

        response.setProfileCompletion(percentage);
        response.setMissingSteps(missing);
        response.setIsComplete(isComplete);
        response.setIsEmailVerified(Boolean.TRUE.equals(profile.getUser().getIsEmailVerified()));
        response.setIsPhoneVerified(Boolean.TRUE.equals(profile.getUser().getIsPhoneVerified()));
        return response;
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
