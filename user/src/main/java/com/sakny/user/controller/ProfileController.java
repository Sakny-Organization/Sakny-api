package com.sakny.user.controller;

import com.sakny.common.dto.*;
import com.sakny.user.entity.User;
import com.sakny.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "User Profiles", description = "Endpoints for managing user profiles, including wizard steps and photo uploads")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "Create a new profile",
            description = "Creates a new profile for the authenticated user. This should be called after completing the profile wizard."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profile created successfully")
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

    @Operation(summary = "Get my profile", description = "Retrieves the profile information of the currently authenticated user.")
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        log.debug("Get profile request for user: {}", user.getEmail());
        ProfileResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update profile", description = "Performs a partial update of the authenticated user's profile.")
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Update profile request for user: {}", user.getEmail());
        ProfileResponse response = profileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @Operation(summary = "Get profile by ID", description = "View another user's profile by their user ID.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(
            @Parameter(description = "ID of the user whose profile to retrieve") @PathVariable Long userId) {
        log.debug("Get profile request for user ID: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update profile photo", description = "Upload or update the user's profile photo. Supports JPEG, PNG, and WebP up to 5MB.")
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfileImage(
            @AuthenticationPrincipal User user,
            @RequestParam("profileImage") MultipartFile profileImage) {
        log.info("Upload profile photo request for user: {}", user.getEmail());
        ProfileResponse response = profileService.updateProfileImage(user.getId(), profileImage);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", response));
    }

    @Operation(summary = "Delete profile photo", description = "Removes the user's current profile photo.")
    @DeleteMapping("/photo")
    public ResponseEntity<ApiResponse<ProfileResponse>> deleteProfileImage(
            @AuthenticationPrincipal User user) {
        log.info("Delete profile photo request for user: {}", user.getEmail());
        ProfileResponse response = profileService.deleteProfileImage(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile photo deleted successfully", response));
    }

    @Operation(summary = "Get contact info", description = "Returns the authenticated user's email, phone, and verification status.")
    @GetMapping("/contact")
    public ResponseEntity<ApiResponse<ContactInfoResponse>> getContactInfo(
            @AuthenticationPrincipal User user) {
        ContactInfoResponse response = profileService.getContactInfo(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Browse roommates", description = "Returns paginated list of complete profiles, excluding the current user. Supports filtering by gender, budget, lifestyle, and roommate type.")
    @GetMapping("/roommates")
    public ResponseEntity<ApiResponse<Page<ProfileResponse>>> getRoommates(
            @AuthenticationPrincipal User user,
            @ParameterObject RoommateFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfileResponse> page = profileService.getRoommates(user.getId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "Save a profile", description = "Bookmarks another user's profile for the authenticated user.")
    @PostMapping("/{userId}/save")
    public ResponseEntity<ApiResponse<Void>> saveProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long userId) {
        profileService.saveProfile(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Profile saved", null));
    }

    @Operation(summary = "Unsave a profile", description = "Removes a bookmarked profile for the authenticated user.")
    @DeleteMapping("/{userId}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long userId) {
        profileService.unsaveProfile(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Profile unsaved", null));
    }

    @Operation(summary = "Get saved profiles", description = "Returns all profiles saved/bookmarked by the authenticated user.")
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<java.util.List<ProfileResponse>>> getSavedProfiles(
            @AuthenticationPrincipal User user) {
        java.util.List<ProfileResponse> saved = profileService.getSavedProfiles(user.getId());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
