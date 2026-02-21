package com.sakny.listing.controller;

import com.sakny.common.dto.*;
import com.sakny.common.model.ListingStatus;
import com.sakny.listing.service.ListingService;
import com.sakny.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/listings")
@RequiredArgsConstructor
@Slf4j
public class ListingController {

    private final ListingService listingService;

    /**
     * Create a new listing.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("request") ListingRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        log.info("Create listing request from user: {}", user.getEmail());
        ListingResponse response = listingService.createListing(user.getId(), request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Listing created successfully", response));
    }

    /**
     * Get a listing by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(@PathVariable Long id) {
        log.debug("Get listing request for ID: {}", id);
        ListingResponse response = listingService.getListing(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update a listing.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequest request) {
        log.info("Update listing request for ID: {} from user: {}", id, user.getEmail());
        ListingResponse response = listingService.updateListing(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Listing updated successfully", response));
    }

    /**
     * Delete a listing.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteListing(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        log.info("Delete listing request for ID: {} from user: {}", id, user.getEmail());
        listingService.deleteListing(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully", null));
    }

    /**
     * Get current user's listings.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getMyListings(
            @AuthenticationPrincipal User user) {
        log.debug("Get my listings request from user: {}", user.getEmail());
        List<ListingResponse> response = listingService.getMyListings(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search listings with filters.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> searchListings(
            @ModelAttribute ListingSearchCriteria criteria) {
        log.debug("Search listings request with criteria: {}", criteria);
        Page<ListingResponse> response = listingService.searchListings(criteria);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Change listing status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ListingResponse>> changeListingStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam ListingStatus status) {
        log.info("Change status request for listing ID: {} to: {}", id, status);
        ListingResponse response = listingService.changeStatus(user.getId(), id, status);
        return ResponseEntity.ok(ApiResponse.success("Listing status updated", response));
    }

    /**
     * Upload images to a listing.
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ListingResponse>> uploadImages(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        log.info("Upload images request for listing ID: {}", id);
        ListingResponse response = listingService.uploadImages(user.getId(), id, images);
        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", response));
    }

    /**
     * Save/bookmark a listing.
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> saveListing(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        log.info("Save listing request for ID: {} from user: {}", id, user.getEmail());
        listingService.saveListing(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Listing saved", null));
    }

    /**
     * Unsave/unbookmark a listing.
     */
    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveListing(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        log.info("Unsave listing request for ID: {} from user: {}", id, user.getEmail());
        listingService.unsaveListing(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Listing unsaved", null));
    }

    /**
     * Get saved listings for current user.
     */
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> getSavedListings(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Get saved listings request from user: {}", user.getEmail());
        Page<ListingResponse> response = listingService.getSavedListings(
                user.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Check if a listing is saved by current user.
     */
    @GetMapping("/{id}/saved")
    public ResponseEntity<ApiResponse<Boolean>> isListingSaved(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        boolean isSaved = listingService.isListingSaved(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(isSaved));
    }
}

