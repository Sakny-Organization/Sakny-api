package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.user.client.MlServiceClient;
import com.sakny.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/price")
@RequiredArgsConstructor
@Tag(name = "Price Estimation", description = "ML-powered price estimation")
public class PriceController {

    private final MlServiceClient mlServiceClient;

    @Operation(summary = "Estimate fair rent price based on profile")
    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimatePrice(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> profileData) {
        Map<String, Object> result = mlServiceClient.estimatePrice(profileData);
        if (result == null) {
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("ML service unavailable"));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Check ML service health")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> mlHealth() {
        boolean healthy = mlServiceClient.isHealthy();
        return ResponseEntity.ok(ApiResponse.success(Map.of("mlServiceHealthy", healthy)));
    }
}
