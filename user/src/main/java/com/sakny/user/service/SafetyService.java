package com.sakny.user.service;

import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ProfileErrorCode;
import com.sakny.user.entity.BlockedUser;
import com.sakny.user.entity.User;
import com.sakny.user.entity.UserReport;
import com.sakny.user.repository.BlockedUserRepository;
import com.sakny.user.repository.UserReportRepository;
import com.sakny.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyService {

    private final BlockedUserRepository blockedUserRepository;
    private final UserReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BusinessException(ProfileErrorCode.USER_NOT_FOUND, "Cannot block yourself");
        }
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }
        User blocker = userRepository.getReferenceById(blockerId);
        User blocked = userRepository.getReferenceById(blockedId);
        blockedUserRepository.save(BlockedUser.builder().blocker(blocker).blocked(blocked).build());
        log.info("User {} blocked user {}", blockerId, blockedId);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        blockedUserRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
        log.info("User {} unblocked user {}", blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getExcludedUserIds(Long userId) {
        List<Long> blocked = blockedUserRepository.findBlockedUserIds(userId);
        List<Long> blockers = blockedUserRepository.findBlockerIds(userId);
        Set<Long> excluded = new HashSet<>(blocked);
        excluded.addAll(blockers);
        return excluded;
    }

    @Transactional(readOnly = true)
    public List<Long> getBlockedUserIds(Long userId) {
        return blockedUserRepository.findBlockedUserIds(userId);
    }

    @Transactional
    public void reportUser(Long reporterId, Long reportedId, String reason, String description) {
        if (reporterId.equals(reportedId)) {
            throw new BusinessException(ProfileErrorCode.USER_NOT_FOUND, "Cannot report yourself");
        }
        if (reportRepository.existsByReporterIdAndReportedId(reporterId, reportedId)) {
            throw new BusinessException(ProfileErrorCode.USER_NOT_FOUND, "You have already reported this user");
        }
        User reporter = userRepository.getReferenceById(reporterId);
        User reported = userRepository.getReferenceById(reportedId);
        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .description(description)
                .build();
        reportRepository.save(report);
        log.info("User {} reported user {} for reason: {}", reporterId, reportedId, reason);
    }
}
