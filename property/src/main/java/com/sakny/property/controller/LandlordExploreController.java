package com.sakny.property.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.property.dto.LandlordMatchResponse;
import com.sakny.property.service.LandlordExploreService;
import com.sakny.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/landlord")
@RequiredArgsConstructor
@Tag(name = "Landlord Explore", description = "Endpoints for landlords to discover and match with potential tenants")
public class LandlordExploreController {

    private final LandlordExploreService landlordExploreService;

    @Operation(summary = "Browse tenants for a property", description = "Returns roommate profiles scored against a specific property's tenant preferences")
    @GetMapping("/properties/{propertyId}/tenants")
    public ResponseEntity<ApiResponse<List<LandlordMatchResponse>>> getTenantMatches(
            @AuthenticationPrincipal User user,
            @PathVariable Long propertyId) {
        List<LandlordMatchResponse> matches = landlordExploreService.getTenantMatches(user.getId(), propertyId);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @Operation(summary = "Get tenant compatibility", description = "Returns detailed compatibility breakdown for a specific tenant against a property")
    @GetMapping("/properties/{propertyId}/tenants/{userId}/compatibility")
    public ResponseEntity<ApiResponse<LandlordMatchResponse>> getTenantCompatibility(
            @AuthenticationPrincipal User user,
            @PathVariable Long propertyId,
            @PathVariable Long userId) {
        LandlordMatchResponse match = landlordExploreService.getTenantCompatibility(user.getId(), propertyId, userId);
        return ResponseEntity.ok(ApiResponse.success(match));
    }

    @Operation(summary = "Get top tenant recommendations", description = "Returns highest-scoring tenant matches across all landlord's available properties")
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<LandlordMatchResponse>>> getRecommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        List<LandlordMatchResponse> recommendations = landlordExploreService.getRecommendations(user.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
