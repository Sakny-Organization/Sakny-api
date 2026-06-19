package com.sakny.user.repository;

import com.sakny.user.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    @Query("SELECT DISTINCT ua.targetUserId FROM UserActivity ua WHERE ua.userId = :userId AND ua.action = 'VIEW' ORDER BY ua.targetUserId")
    List<Long> findViewedUserIds(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ua.targetUserId FROM UserActivity ua WHERE ua.userId = :userId AND ua.action IN ('SAVE', 'MESSAGE') AND ua.createdAt > :since")
    List<Long> findEngagedUserIds(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.targetUserId = :targetUserId AND ua.action = 'VIEW' AND ua.createdAt > :since")
    long countRecentViews(@Param("targetUserId") Long targetUserId, @Param("since") LocalDateTime since);
}
