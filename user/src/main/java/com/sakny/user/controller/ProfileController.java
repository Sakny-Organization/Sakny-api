package com.sakny.user.controller;

import com.sakny.common.dto.*;
import com.sakny.user.entity.User;
import com.sakny.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Create a new profile for the authenticated user.
     * Called after completing the 6-step profile wizard.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("request") ProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("Create profile request for user: {}", user.getEmail());
        ProfileResponse response = profileService.createProfile(user.getId(), request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", response));
    }

    /**
     * Get the authenticated user's own profile.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        log.debug("Get profile request for user: {}", user.getEmail());
        ProfileResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Partial update of the authenticated user's profile.
     * Only provided fields will be updated.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Update profile request for user: {}", user.getEmail());
        ProfileResponse response = profileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * View another user's profile (for match viewing).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(
            @PathVariable Long userId) {
        log.debug("Get profile request for user ID: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Upload or update profile photo.
     * Accepts JPEG, PNG, and WebP images up to 5MB.
     */
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfileImage(
            @AuthenticationPrincipal User user,
            @RequestParam("profileImage") MultipartFile profileImage) {
        log.info("Upload profile photo request for user: {}", user.getEmail());
        ProfileResponse response = profileService.updateProfileImage(user.getId(), profileImage);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", response));
    }

    /**
     * Delete the current profile photo.
     */
    @DeleteMapping("/photo")
    public ResponseEntity<ApiResponse<ProfileResponse>> deleteProfileImage(
            @AuthenticationPrincipal User user) {
        log.info("Delete profile photo request for user: {}", user.getEmail());
        ProfileResponse response = profileService.deleteProfileImage(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile photo deleted successfully", response));
    }

    /**
     * Get the authenticated user's own contact info.
     */
    @GetMapping("/contact")
    public ResponseEntity<ApiResponse<ContactInfoResponse>> getMyContactInfo(
            @AuthenticationPrincipal User user) {
        log.debug("Get own contact info request for user: {}", user.getEmail());
        ContactInfoResponse response = profileService.getContactInfo(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
