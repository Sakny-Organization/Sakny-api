package com.sakny.property.listener;

import com.sakny.property.entity.Property;
import com.sakny.property.repository.PropertyRepository;
import com.sakny.property.service.LandlordMatchingService;
import com.sakny.user.entity.UserProfile;
import com.sakny.user.event.ProfileUpdatedEvent;
import com.sakny.user.repository.UserProfileRepository;
import com.sakny.user.service.MatchingService.MatchScoreResult;
import com.sakny.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantMatchNotificationListener {

    private static final double HIGH_MATCH_THRESHOLD = 75.0;

    private final PropertyRepository propertyRepository;
    private final UserProfileRepository userProfileRepository;
    private final LandlordMatchingService matchingService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void onProfileUpdated(ProfileUpdatedEvent event) {
        try {
            UserProfile candidate = userProfileRepository.findByUserIdWithDetails(event.userId())
                    .orElse(null);

            if (candidate == null || !Boolean.TRUE.equals(candidate.getIsComplete())) {
                return;
            }

            List<Property> availableProperties = propertyRepository.findAll().stream()
                    .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                    .filter(p -> p.getOwner() != null && !p.getOwner().getId().equals(event.userId()))
                    .toList();

            for (Property property : availableProperties) {
                MatchScoreResult result = matchingService.computePropertyMatchScore(property, candidate);

                if (result.score() >= HIGH_MATCH_THRESHOLD) {
                    String candidateName = candidate.getUser() != null ? candidate.getUser().getName() : "A tenant";
                    notificationService.createNotification(
                            property.getOwner().getId(),
                            "match",
                            "New high-compatibility tenant!",
                            String.format("%s has %.0f%% compatibility with your property \"%s\"",
                                    candidateName, result.score(), property.getTitle()),
                            event.userId()
                    );
                    log.info("Notified landlord {} about high-match tenant {} (score: {}) for property {}",
                            property.getOwner().getId(), event.userId(), result.score(), property.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing tenant match notifications for user {}: {}", event.userId(), e.getMessage(), e);
        }
    }
}
