package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.common.dto.LocationDto;
import com.sakny.user.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Locations", description = "Public endpoints for looking up Egyptian governorates and cities")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Get all governorates", description = "Retrieves a listing of all Egyptian governorates. This is a public endpoint.")
    @GetMapping("/governorates")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAllGovernorates() {
        log.debug("Fetching all governorates");
        List<LocationDto> governorates = locationService.getAllGovernorates();
        return ResponseEntity.ok(ApiResponse.success(governorates));
    }

    @Operation(summary = "Get cities by governorate", description = "Retrieves all cities belonging to a specific governorate ID. This is a public endpoint.")
    @GetMapping("/governorates/{governorateId}/cities")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getCitiesByGovernorate(
            @Parameter(description = "ID of the governorate to fetch cities for") @PathVariable Integer governorateId) {
        log.debug("Fetching cities for governorate ID: {}", governorateId);
        List<LocationDto> cities = locationService.getCitiesByGovernorate(governorateId);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}
