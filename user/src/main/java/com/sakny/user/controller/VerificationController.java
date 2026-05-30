package com.sakny.user.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.common.dto.VerificationStatusResponse;
import com.sakny.user.entity.User;
import com.sakny.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/v1/verification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC Verification", description = "Egyptian National ID verification endpoints")
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(
            summary = "Submit ID verification",
            description = "Upload front of National ID, back of National ID, and a selfie for KYC verification."
    )
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> submitVerification(
            @AuthenticationPrincipal User user,
            @RequestParam("frontId") MultipartFile frontId,
            @RequestParam("backId") MultipartFile backId,
            @RequestParam("selfie") MultipartFile selfie) {

        log.info("Verification submission request from user {}", user.getId());
        VerificationStatusResponse response =
                verificationService.submitVerification(user.getId(), frontId, backId, selfie);
        return ResponseEntity.ok(ApiResponse.success("Verification submitted successfully", response));
    }

    @Operation(
            summary = "Get verification status",
            description = "Returns the current KYC verification status for the authenticated user."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> getStatus(
            @AuthenticationPrincipal User user) {
        VerificationStatusResponse response = verificationService.getStatus(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Shufti Pro webhook",
            description = "Public endpoint called by Shufti Pro to deliver async verification results. No authentication required."
    )
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Shufti Pro webhook, event={}", payload.get("event"));
        verificationService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
