package com.sakny.property.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.property.dto.PropertyFilterRequest;
import com.sakny.property.dto.PropertyRequest;
import com.sakny.property.dto.PropertyResponse;
import com.sakny.property.service.PropertyService;
import com.sakny.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@RestController
@RequestMapping("/v1/properties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Properties", description = "Endpoints for managing property listings")
public class PropertyController {

    private final PropertyService propertyService;

    @Operation(summary = "Create property", description = "Create a new property listing. Accepts multipart/form-data with a JSON 'request' part and optional image files.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("request") PropertyRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        PropertyResponse response = propertyService.createProperty(user.getId(), request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", response));
    }

    @Operation(summary = "Browse properties", description = "Returns paginated property listings with optional filters.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getProperties(
            @ParameterObject PropertyFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PropertyResponse> page = propertyService.getProperties(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "Get property by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.getProperty(id)));
    }

    @Operation(summary = "Update property", description = "Update an existing property. Only the owner can update.")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully",
                propertyService.updateProperty(user.getId(), id, request)));
    }

    @Operation(summary = "Delete property", description = "Delete a property and all its images. Only the owner can delete.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        propertyService.deleteProperty(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }

    @Operation(summary = "Upload property images")
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PropertyResponse>> addImages(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.addImages(user.getId(), id, images)));
    }

    @Operation(summary = "Delete property image")
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ApiResponse<PropertyResponse>> deleteImage(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.deleteImage(user.getId(), id, imageId)));
    }

    @Operation(summary = "Get my listings", description = "Returns all properties owned by the authenticated user.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyProperties(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.getMyProperties(user.getId())));
    }

    @Operation(summary = "Toggle property status", description = "Toggle between AVAILABLE and RENTED. Only the owner can toggle.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PropertyResponse>> toggleStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                propertyService.toggleStatus(user.getId(), id)));
    }
}
