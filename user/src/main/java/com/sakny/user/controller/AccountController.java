package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.user.entity.User;
import com.sakny.user.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Delete user account and all associated data")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal User user) {
        accountService.deleteAccount(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @Operation(summary = "Export user data (GDPR)")
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<Object>> exportData(
            @AuthenticationPrincipal User user) {
        Object data = accountService.exportUserData(user.getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
