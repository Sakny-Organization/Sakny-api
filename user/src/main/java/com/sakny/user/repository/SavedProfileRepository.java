package com.sakny.user.repository;

import com.sakny.user.entity.SavedProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedProfileRepository extends JpaRepository<SavedProfile, Long> {

    boolean existsByUser_IdAndSavedUser_Id(Long userId, Long savedUserId);

    Optional<SavedProfile> findByUser_IdAndSavedUser_Id(Long userId, Long savedUserId);

    @Query("SELECT sp.savedUser.id FROM SavedProfile sp WHERE sp.user.id = :userId")
    List<Long> findSavedUserIdsByUserId(@Param("userId") Long userId);

    List<SavedProfile> findByUser_Id(Long userId);

    void deleteByUser_IdAndSavedUser_Id(Long userId, Long savedUserId);
}
