package com.sakny.listing.repository;

import com.sakny.listing.entity.SavedListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, Long> {

    /**
     * Find all saved listings for a user.
     */
    @Query("SELECT sl FROM SavedListing sl " +
            "JOIN FETCH sl.listing l " +
            "LEFT JOIN FETCH l.governorate " +
            "LEFT JOIN FETCH l.city " +
            "WHERE sl.user.id = :userId " +
            "ORDER BY sl.savedAt DESC")
    Page<SavedListing> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    /**
     * Check if a listing is saved by user.
     */
    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    /**
     * Find saved listing by user and listing.
     */
    Optional<SavedListing> findByUserIdAndListingId(Long userId, Long listingId);

    /**
     * Delete saved listing by user and listing.
     */
    void deleteByUserIdAndListingId(Long userId, Long listingId);

    /**
     * Get all saved listing IDs for a user.
     */
    @Query("SELECT sl.listing.id FROM SavedListing sl WHERE sl.user.id = :userId")
    List<Long> findListingIdsByUserId(@Param("userId") Long userId);

    /**
     * Count saved listings for a user.
     */
    long countByUserId(Long userId);
}

