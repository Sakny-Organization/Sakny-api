package com.sakny.property.service;

import com.sakny.common.exception.BusinessException;
import com.sakny.property.dto.PropertyResponse;
import com.sakny.property.dto.TenantPropertyMatchResponse;
import com.sakny.property.entity.Property;
import com.sakny.property.exception.PropertyErrorCode;
import com.sakny.property.mapper.PropertyMapper;
import com.sakny.property.repository.PropertyRepository;
import com.sakny.user.entity.UserProfile;
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
public class TenantExploreService {

    private final PropertyRepository propertyRepository;
    private final UserProfileRepository userProfileRepository;
    private final LandlordMatchingService matchingService;
    private final MatchExplanationService explanationService;
    private final PropertyMapper propertyMapper;

    @Transactional(readOnly = true)
    public List<TenantPropertyMatchResponse> getMatchedProperties(Long userId) {
        UserProfile tenant = userProfileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.USER_NOT_FOUND));

        List<Property> availableProperties = propertyRepository.findAll().stream()
                .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                .filter(p -> !p.getOwner().getId().equals(userId))
                .toList();

        return availableProperties.stream()
                .map(property -> buildMatchResponse(property, tenant))
                .filter(r -> r.getScore() > 0)
                .sorted(Comparator.comparingDouble(TenantPropertyMatchResponse::getScore).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantPropertyMatchResponse getPropertyCompatibility(Long userId, Long propertyId) {
        UserProfile tenant = userProfileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.USER_NOT_FOUND));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        return buildMatchResponse(property, tenant);
    }

    private TenantPropertyMatchResponse buildMatchResponse(Property property, UserProfile tenant) {
        MatchScoreResult result = matchingService.computePropertyMatchScore(property, tenant);

        PropertyResponse propertyResponse = propertyMapper.toResponse(property);
        String tenantName = tenant.getUser() != null ? tenant.getUser().getName() : "Tenant";
        String explanation = explanationService.generateExplanation(
                result.score(), result.breakdown(), tenantName);
        List<String> topics = explanationService.generateDiscussionTopics(result.breakdown());

        return TenantPropertyMatchResponse.builder()
                .propertyId(property.getId())
                .score(result.score())
                .breakdown(result.breakdown())
                .strengths(result.strengths())
                .conflicts(result.conflicts())
                .explanation(explanation)
                .discussionTopics(topics)
                .property(propertyResponse)
                .build();
    }
}
