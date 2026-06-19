package com.sakny.user.repository;

import com.sakny.user.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("SELECT b.blocked.id FROM BlockedUser b WHERE b.blocker.id = :userId")
    List<Long> findBlockedUserIds(@Param("userId") Long userId);

    @Query("SELECT b.blocker.id FROM BlockedUser b WHERE b.blocked.id = :userId")
    List<Long> findBlockerIds(@Param("userId") Long userId);
}
