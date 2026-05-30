package com.sakny.user.service;

import com.sakny.common.dto.VerificationStatusResponse;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.VerificationErrorCode;
import com.sakny.common.model.VerificationStatus;
import com.sakny.common.service.StorageService;
import com.sakny.user.client.ShuftiProClient;
import com.sakny.user.entity.User;
import com.sakny.user.entity.VerificationSubmission;
import com.sakny.user.repository.UserRepository;
import com.sakny.user.repository.VerificationSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final VerificationSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ShuftiProClient shuftiProClient;

    @Transactional
    public VerificationStatusResponse submitVerification(
            Long userId,
            MultipartFile frontId,
            MultipartFile backId,
            MultipartFile selfie) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_ALREADY_APPROVED);
        }

        if (submissionRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING)) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_ALREADY_PENDING);
        }

        // Upload documents to MinIO
        String frontIdUrl = storageService.uploadVerificationDocument(frontId, userId, "front-id");
        String backIdUrl  = storageService.uploadVerificationDocument(backId,  userId, "back-id");
        String selfieUrl  = storageService.uploadVerificationDocument(selfie,  userId, "selfie");

        String reference = "sakny-" + userId + "-" + UUID.randomUUID();

        VerificationSubmission submission = VerificationSubmission.builder()
                .user(user)
                .status(VerificationStatus.PENDING)
                .frontIdUrl(frontIdUrl)
                .backIdUrl(backIdUrl)
                .selfieUrl(selfieUrl)
                .externalReference(reference)
                .build();
        submissionRepository.save(submission);

        // Call Shufti Pro; handle synchronous result if returned immediately
        try {
            String event = shuftiProClient.submitVerification(
                    encodeToBase64(frontId),
                    encodeToBase64(backId),
                    encodeToBase64(selfie),
                    reference);

            if ("verification.accepted".equals(event)) {
                approveUser(user, submission);
            } else if ("verification.declined".equals(event)) {
                submission.setStatus(VerificationStatus.REJECTED);
                submission.setResolvedAt(LocalDateTime.now());
                submissionRepository.save(submission);
            }
            // "request.pending" → webhook will arrive later
        } catch (BusinessException e) {
            // External API failure — leave submission PENDING, webhook may still arrive
            log.warn("Shufti Pro call failed; submission {} remains PENDING: {}", submission.getId(), e.getMessage());
        }

        return toResponse(submission, user.getIsVerified());
    }

    /**
     * Called by Shufti Pro webhook. Idempotent — safe to receive duplicate calls.
     */
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        String reference = (String) payload.get("reference");
        String event     = (String) payload.get("event");

        if (reference == null || event == null) {
            log.warn("Webhook received with null reference or event: {}", payload);
            return;
        }

        VerificationSubmission submission = submissionRepository.findByExternalReference(reference)
                .orElse(null);
        if (submission == null) {
            log.warn("Webhook reference not found: {}", reference);
            return;
        }

        if (submission.getStatus() != VerificationStatus.PENDING) {
            log.info("Webhook for already-resolved submission {}, ignoring", submission.getId());
            return;
        }

        if ("verification.accepted".equals(event)) {
            approveUser(submission.getUser(), submission);
        } else if ("verification.declined".equals(event)) {
            submission.setStatus(VerificationStatus.REJECTED);
            submission.setResolvedAt(LocalDateTime.now());
            submissionRepository.save(submission);
            log.info("Verification rejected for user {}, submission {}",
                    submission.getUser().getId(), submission.getId());
        }
    }

    @Transactional(readOnly = true)
    public VerificationStatusResponse getStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.USER_NOT_FOUND));

        Optional<VerificationSubmission> latest =
                submissionRepository.findTopByUserIdOrderBySubmittedAtDesc(userId);

        if (latest.isEmpty()) {
            return VerificationStatusResponse.builder()
                    .status(null)
                    .isVerified(user.getIsVerified())
                    .build();
        }
        return toResponse(latest.get(), user.getIsVerified());
    }

    // ===== Helpers =====

    private void approveUser(User user, VerificationSubmission submission) {
        submission.setStatus(VerificationStatus.APPROVED);
        submission.setResolvedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        user.setIsVerified(true);
        userRepository.save(user);
        log.info("User {} verified successfully, submission {}", user.getId(), submission.getId());
    }

    private VerificationStatusResponse toResponse(VerificationSubmission s, Boolean isVerified) {
        return VerificationStatusResponse.builder()
                .submissionId(s.getId())
                .status(s.getStatus())
                .submittedAt(s.getSubmittedAt())
                .resolvedAt(s.getResolvedAt())
                .isVerified(isVerified)
                .build();
    }

    private String encodeToBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception e) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_UPLOAD_FAILED,
                    "Failed to encode document: " + e.getMessage());
        }
    }
}
