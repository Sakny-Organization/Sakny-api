package com.sakny.user.service;

import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ProfileErrorCode;
import com.sakny.user.entity.User;
import com.sakny.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final NotificationRepository notificationRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserReportRepository reportRepository;
    private final UserActivityRepository activityRepository;
    private final SavedProfileRepository savedProfileRepository;
    private final VerificationSubmissionRepository verificationSubmissionRepository;

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));

        // Delete associated data in order (respecting FK constraints)
        activityRepository.deleteAll(activityRepository.findAll().stream()
                .filter(a -> a.getUserId().equals(userId) || a.getTargetUserId().equals(userId))
                .toList());

        notificationRepository.deleteAll(notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(userId))
                .toList());

        blockedUserRepository.deleteAll(blockedUserRepository.findAll().stream()
                .filter(b -> b.getBlocker().getId().equals(userId) || b.getBlocked().getId().equals(userId))
                .toList());

        reportRepository.deleteAll(reportRepository.findAll().stream()
                .filter(r -> r.getReporter().getId().equals(userId) || r.getReported().getId().equals(userId))
                .toList());

        savedProfileRepository.deleteAll(savedProfileRepository.findAll().stream()
                .filter(sp -> sp.getUser().getId().equals(userId) || sp.getSavedUser().getId().equals(userId))
                .toList());

        verificationSubmissionRepository.findTopByUserIdOrderBySubmittedAtDesc(userId)
                .ifPresent(verificationSubmissionRepository::delete);

        // Delete profile if exists
        profileRepository.findByUserId(userId).ifPresent(profileRepository::delete);

        // Delete user
        userRepository.delete(user);

        log.info("Account deleted for user {}", userId);
    }

    @Transactional(readOnly = true)
    public Object exportUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.USER_NOT_FOUND));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("account", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        ));

        profileRepository.findByUserId(userId).ifPresent(profile -> {
            data.put("profile", Map.of(
                    "age", profile.getAge() != null ? profile.getAge() : "",
                    "gender", profile.getGender() != null ? profile.getGender().name() : "",
                    "occupation", profile.getOccupation() != null ? profile.getOccupation() : "",
                    "bio", profile.getBio() != null ? profile.getBio() : "",
                    "budgetMin", profile.getBudgetMin() != null ? profile.getBudgetMin() : "",
                    "budgetMax", profile.getBudgetMax() != null ? profile.getBudgetMax() : ""
            ));
        });

        return data;
    }
}
