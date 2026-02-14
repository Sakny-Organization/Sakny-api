package com.sakny.user.repository;

import com.sakny.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("SELECT p FROM UserProfile p " +
            "LEFT JOIN FETCH p.preferredAreas " +
            "LEFT JOIN FETCH p.currentGovernorate " +
            "LEFT JOIN FETCH p.currentCity " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.user.id = :userId")
    Optional<UserProfile> findByUserIdWithDetails(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    Optional<UserProfile> findByUserId(Long userId);
}
