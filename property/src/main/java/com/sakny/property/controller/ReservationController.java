package com.sakny.property.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.property.dto.ReservationRequest;
import com.sakny.property.dto.ReservationResponse;
import com.sakny.property.entity.ReservationStatus;
import com.sakny.property.service.ReservationService;
import com.sakny.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservations", description = "Endpoints for managing property reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a new reservation", description = "Allows a user to reserve a property for specific dates")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReservationRequest request) {
        log.info("Received reservation request from user {} for property {}", user.getId(), request.getPropertyId());
        ReservationResponse response = reservationService.createReservation(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Reservation created successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user reservations", description = "Retrieves a list of all reservations made by the authenticated user")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal User user) {
        log.info("Fetching reservations for user {}", user.getId());
        List<ReservationResponse> response = reservationService.getUserReservations(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/owner")
    @Operation(summary = "Get reservations for owned properties", description = "Retrieves bookings for properties owned by the authenticated user")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getOwnerReservations(
            @AuthenticationPrincipal User user) {
        log.info("Fetching owner reservations for user {}", user.getId());
        List<ReservationResponse> response = reservationService.getOwnerReservations(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Allows a user to cancel their own reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        log.info("Cancel request for reservation {} by user {}", id, user.getId());
        ReservationResponse response = reservationService.cancelReservation(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status", description = "Allows a property owner to confirm or reject a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {
        log.info("Status update request for reservation {} to {} by owner {}", id, status, user.getId());
        ReservationResponse response = reservationService.updateStatus(user.getId(), id, status);
        return ResponseEntity.ok(ApiResponse.success("Reservation status updated successfully", response));
    }
}
