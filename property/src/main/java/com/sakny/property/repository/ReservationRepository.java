package com.sakny.property.repository;

import com.sakny.property.entity.Reservation;
import com.sakny.property.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByPropertyId(Long propertyId);
    List<Reservation> findByPropertyOwnerId(Long ownerId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.property.id = :propertyId " +
           "AND r.status NOT IN (:excludedStatuses) " +
           "AND r.startDate < :endDate AND r.endDate > :startDate")
    boolean existsOverlappingReservation(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedStatuses") Collection<ReservationStatus> excludedStatuses
    );
}
