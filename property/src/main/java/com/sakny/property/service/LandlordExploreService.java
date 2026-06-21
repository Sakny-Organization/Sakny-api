package com.sakny.property.service;

import com.sakny.common.dto.ProfileResponse;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.model.Gender;
import com.sakny.common.model.HousingRole;
import com.sakny.property.dto.LandlordMatchResponse;
import com.sakny.property.entity.Property;
import com.sakny.property.exception.PropertyErrorCode;
import com.sakny.property.repository.PropertyRepository;
import com.sakny.user.entity.UserProfile;
import com.sakny.user.mapper.ProfileMapper;
import com.sakny.user.repository.UserProfileRepository;
import com.sakny.user.service.MatchExplanationService;
import com.sakny.user.service.MatchingService.MatchScoreResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandlordExploreService {

    private final PropertyRepository propertyRepository;
    private final UserProfileRepository userProfileRepository;
    private final LandlordMatchingService matchingService;
    private final MatchExplanationService explanationService;
    private final ProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public List<LandlordMatchResponse> getTenantMatches(Long ownerId, Long propertyId) {
        Property property = getOwnedProperty(ownerId, propertyId);

        List<UserProfile> candidates = userProfileRepository.findAll().stream()
                .filter(p -> p.getUser() != null)
                .filter(p -> p.getUser().getHousingRole() == HousingRole.ROOMMATE)
                .filter(p -> !p.getUser().getId().equals(ownerId))
                .filter(p -> Boolean.TRUE.equals(p.getIsComplete()))
                .toList();

        return candidates.stream()
                .map(candidate -> buildMatchResponse(property, candidate))
                .filter(r -> r.getScore() > 0)
                .sorted(Comparator.comparingDouble(LandlordMatchResponse::getScore).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public LandlordMatchResponse getTenantCompatibility(Long ownerId, Long propertyId, Long userId) {
        Property property = getOwnedProperty(ownerId, propertyId);

        UserProfile candidate = userProfileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.USER_NOT_FOUND));

        return buildMatchResponse(property, candidate);
    }

    @Transactional(readOnly = true)
    public List<LandlordMatchResponse> getRecommendations(Long ownerId, int limit) {
        List<Property> properties = propertyRepository.findByOwnerId(ownerId).stream()
                .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                .toList();

        if (properties.isEmpty()) return List.of();

        List<UserProfile> candidates = userProfileRepository.findAll().stream()
                .filter(p -> p.getUser() != null)
                .filter(p -> p.getUser().getHousingRole() == HousingRole.ROOMMATE)
                .filter(p -> !p.getUser().getId().equals(ownerId))
                .filter(p -> Boolean.TRUE.equals(p.getIsComplete()))
                .toList();

        return properties.stream()
                .flatMap(property -> candidates.stream()
                        .map(candidate -> buildMatchResponse(property, candidate)))
                .filter(r -> r.getScore() > 0)
                .sorted(Comparator.comparingDouble(LandlordMatchResponse::getScore).reversed())
                .limit(limit)
                .toList();
    }

    private LandlordMatchResponse buildMatchResponse(Property property, UserProfile candidate) {
        MatchScoreResult result = matchingService.computePropertyMatchScore(property, candidate);

        ProfileResponse profileResponse = profileMapper.toResponse(candidate);
        String candidateName = candidate.getUser() != null ? candidate.getUser().getName() : "Tenant";
        String explanation = explanationService.generateExplanation(
                result.score(), result.breakdown(), candidateName);
        List<String> topics = explanationService.generateDiscussionTopics(result.breakdown());

        return LandlordMatchResponse.builder()
                .propertyId(property.getId())
                .propertyTitle(property.getTitle())
                .userId(candidate.getUser().getId())
                .score(result.score())
                .breakdown(result.breakdown())
                .strengths(result.strengths())
                .conflicts(result.conflicts())
                .explanation(explanation)
                .discussionTopics(topics)
                .profile(profileResponse)
                .build();
    }

    private Property getOwnedProperty(Long ownerId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.PROPERTY_NOT_FOUND));
        if (!property.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(PropertyErrorCode.NOT_PROPERTY_OWNER);
        }
        return property;
    }
}
