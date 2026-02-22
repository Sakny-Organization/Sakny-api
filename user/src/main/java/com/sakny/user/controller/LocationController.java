package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.common.dto.LocationDto;
import com.sakny.user.service.LocationService;
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
public class LocationController {

    private final LocationService locationService;

    /**
     * Get all Egyptian governorates.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/governorates")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAllGovernorates() {
        log.debug("Fetching all governorates");
        List<LocationDto> governorates = locationService.getAllGovernorates();
        return ResponseEntity.ok(ApiResponse.success(governorates));
    }

    /**
     * Get all cities within a specific governorate.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/governorates/{governorateId}/cities")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getCitiesByGovernorate(
            @PathVariable Integer governorateId) {
        log.debug("Fetching cities for governorate ID: {}", governorateId);
        List<LocationDto> cities = locationService.getCitiesByGovernorate(governorateId);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}
