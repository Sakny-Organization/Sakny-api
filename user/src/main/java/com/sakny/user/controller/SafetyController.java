package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.user.entity.User;
import com.sakny.user.service.SafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/safety")
@RequiredArgsConstructor
@Tag(name = "Safety", description = "Block and report users")
public class SafetyController {

    private final SafetyService safetyService;

    @Operation(summary = "Block a user")
    @PostMapping("/block/{userId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long userId) {
        safetyService.blockUser(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("User blocked", null));
    }

    @Operation(summary = "Unblock a user")
    @DeleteMapping("/block/{userId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long userId) {
        safetyService.unblockUser(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", null));
    }

    @Operation(summary = "Get blocked users list")
    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse<List<Long>>> getBlockedUsers(
            @AuthenticationPrincipal User user) {
        List<Long> blockedIds = safetyService.getBlockedUserIds(user.getId());
        return ResponseEntity.ok(ApiResponse.success(blockedIds));
    }

    @Operation(summary = "Report a user")
    @PostMapping("/report/{userId}")
    public ResponseEntity<ApiResponse<Void>> reportUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long userId,
            @RequestParam @NotBlank String reason,
            @RequestParam(required = false) String description) {
        safetyService.reportUser(user.getId(), userId, reason, description);
        return ResponseEntity.ok(ApiResponse.success("User reported", null));
    }
}
