package com.sakny.user.repository;

import com.sakny.user.entity.SavedProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedProfileRepository extends JpaRepository<SavedProfile, Long> {

    boolean existsByUserIdAndSavedUserId(Long userId, Long savedUserId);

    Optional<SavedProfile> findByUserIdAndSavedUserId(Long userId, Long savedUserId);

    @Query("SELECT sp.savedUser.id FROM SavedProfile sp WHERE sp.user.id = :userId")
    List<Long> findSavedUserIdsByUserId(@Param("userId") Long userId);

    List<SavedProfile> findByUserId(Long userId);

    void deleteByUserIdAndSavedUserId(Long userId, Long savedUserId);
}
