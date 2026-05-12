package com.sakny.property.service;

import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ReservationErrorCode;
import com.sakny.property.dto.ReservationRequest;
import com.sakny.property.dto.ReservationResponse;
import com.sakny.property.entity.Property;
import com.sakny.property.entity.Reservation;
import com.sakny.property.entity.ReservationStatus;
import com.sakny.property.mapper.ReservationMapper;
import com.sakny.property.repository.PropertyRepository;
import com.sakny.property.repository.ReservationRepository;
import com.sakny.user.entity.User;
import com.sakny.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        log.info("Creating reservation for user {} on property {}", userId, request.getPropertyId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.UNAUTHORIZED_RESERVATION_ACCESS));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // Owners cannot reserve their own property
        if (property.getOwner().getId().equals(userId)) {
            throw new BusinessException(ReservationErrorCode.OWNER_CANNOT_RESERVE_OWN_PROPERTY);
        }

        validateDates(request.getStartDate(), request.getEndDate());

        // Check availability
        boolean isOverlapping = reservationRepository.existsOverlappingReservation(
                property.getId(),
                request.getStartDate(),
                request.getEndDate(),
                Arrays.asList(ReservationStatus.CANCELLED, ReservationStatus.REJECTED)
        );

        if (isOverlapping) {
            throw new BusinessException(ReservationErrorCode.PROPERTY_NOT_AVAILABLE);
        }

        BigDecimal totalPrice = calculateTotalPrice(property.getPrice(), request.getStartDate(), request.getEndDate());

        Reservation reservation = reservationMapper.toEntity(request, property, user, totalPrice);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation created with ID: {}", saved.getId());
        return reservationMapper.toResponse(saved);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(ReservationErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.REJECTED) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS_CHANGE);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse updateStatus(Long ownerId, Long reservationId, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getProperty().getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ReservationErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        if (newStatus != ReservationStatus.APPROVED && newStatus != ReservationStatus.REJECTED) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS_CHANGE);
        }

        reservation.setStatus(newStatus);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        return reservationMapper.toResponseList(reservationRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getOwnerReservations(Long ownerId) {
        return reservationMapper.toResponseList(reservationRepository.findByPropertyOwnerId(ownerId));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start.isBefore(LocalDate.now())) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_DATES, "Start date cannot be in the past");
        }
        if (!start.isBefore(end)) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_DATES, "Start date must be before end date");
        }
    }

    private BigDecimal calculateTotalPrice(BigDecimal pricePerNight, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1; // Minimum 1 day/night
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }
}
