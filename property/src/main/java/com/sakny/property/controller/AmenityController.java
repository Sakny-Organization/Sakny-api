package com.sakny.property.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.property.dto.AmenityResponse;
import com.sakny.property.service.AmenityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/amenities")
@RequiredArgsConstructor
@Tag(name = "Amenities", description = "Pre-seeded property amenities for selection")
public class AmenityController {

    private final AmenityService amenityService;

    @Operation(summary = "Get all amenities", description = "Returns all available property amenities for use when creating or filtering listings.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> getAllAmenities() {
        return ResponseEntity.ok(ApiResponse.success(amenityService.getAllAmenities()));
    }
}
