package com.sakny.user.service;

import com.sakny.user.entity.UserActivity;
import com.sakny.user.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final UserActivityRepository activityRepository;

    @Transactional
    public void trackView(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) return;
        activityRepository.save(UserActivity.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .action("VIEW")
                .build());
    }

    @Transactional
    public void trackSave(Long userId, Long targetUserId) {
        activityRepository.save(UserActivity.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .action("SAVE")
                .build());
    }

    @Transactional
    public void trackMessage(Long userId, Long targetUserId) {
        activityRepository.save(UserActivity.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .action("MESSAGE")
                .build());
    }
}
