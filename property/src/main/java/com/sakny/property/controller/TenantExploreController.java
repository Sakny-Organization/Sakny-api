package com.sakny.property.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.property.dto.TenantPropertyMatchResponse;
import com.sakny.property.service.TenantExploreService;
import com.sakny.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tenant")
@RequiredArgsConstructor
@Tag(name = "Tenant Explore", description = "Endpoints for tenants/roommates to discover properties matched to their preferences")
public class TenantExploreController {

    private final TenantExploreService tenantExploreService;

    @Operation(summary = "Browse matched properties", description = "Returns properties scored against the tenant's preferences, ranked by compatibility")
    @GetMapping("/properties/matched")
    public ResponseEntity<ApiResponse<List<TenantPropertyMatchResponse>>> getMatchedProperties(
            @AuthenticationPrincipal User user) {
        List<TenantPropertyMatchResponse> matches = tenantExploreService.getMatchedProperties(user.getId());
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @Operation(summary = "Get property compatibility", description = "Returns detailed compatibility breakdown for a specific property against tenant's preferences")
    @GetMapping("/properties/{propertyId}/compatibility")
    public ResponseEntity<ApiResponse<TenantPropertyMatchResponse>> getPropertyCompatibility(
            @AuthenticationPrincipal User user,
            @PathVariable Long propertyId) {
        TenantPropertyMatchResponse match = tenantExploreService.getPropertyCompatibility(user.getId(), propertyId);
        return ResponseEntity.ok(ApiResponse.success(match));
    }
}
