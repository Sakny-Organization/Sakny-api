package com.sakny.listing.repository;

import com.sakny.common.model.ListingStatus;
import com.sakny.listing.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    /**
     * Find listing by ID with all details eagerly loaded.
     */
    @Query("SELECT l FROM Listing l " +
            "LEFT JOIN FETCH l.user u " +
            "LEFT JOIN FETCH l.governorate " +
            "LEFT JOIN FETCH l.city " +
            "LEFT JOIN FETCH l.images " +
            "WHERE l.id = :id")
    Optional<Listing> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all listings by user ID.
     */
    @Query("SELECT l FROM Listing l " +
            "LEFT JOIN FETCH l.governorate " +
            "LEFT JOIN FETCH l.city " +
            "WHERE l.user.id = :userId " +
            "ORDER BY l.createdAt DESC")
    List<Listing> findByUserId(@Param("userId") Long userId);

    /**
     * Find all active listings.
     */
    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    /**
     * Find active listings by governorate.
     */
    @Query("SELECT l FROM Listing l " +
            "WHERE l.status = :status " +
            "AND l.governorate.id = :governorateId " +
            "ORDER BY l.createdAt DESC")
    Page<Listing> findByStatusAndGovernorateId(
            @Param("status") ListingStatus status,
            @Param("governorateId") Integer governorateId,
            Pageable pageable);

    /**
     * Find active listings by city.
     */
    @Query("SELECT l FROM Listing l " +
            "WHERE l.status = :status " +
            "AND l.city.id = :cityId " +
            "ORDER BY l.createdAt DESC")
    Page<Listing> findByStatusAndCityId(
            @Param("status") ListingStatus status,
            @Param("cityId") Integer cityId,
            Pageable pageable);

    /**
     * Find active listings within budget range.
     */
    @Query("SELECT l FROM Listing l " +
            "WHERE l.status = 'ACTIVE' " +
            "AND l.rentAmount >= :minRent " +
            "AND l.rentAmount <= :maxRent " +
            "ORDER BY l.createdAt DESC")
    Page<Listing> findByRentAmountBetween(
            @Param("minRent") Integer minRent,
            @Param("maxRent") Integer maxRent,
            Pageable pageable);

    /**
     * Count listings by user ID.
     */
    long countByUserId(Long userId);

    /**
     * Count active listings by user ID.
     */
    long countByUserIdAndStatus(Long userId, ListingStatus status);

    /**
     * Check if a listing exists and is owned by user.
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}

