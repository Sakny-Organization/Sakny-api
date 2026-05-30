package com.sakny.user.repository;

import com.sakny.common.model.VerificationStatus;
import com.sakny.user.entity.VerificationSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationSubmissionRepository extends JpaRepository<VerificationSubmission, Long> {

    Optional<VerificationSubmission> findTopByUserIdOrderBySubmittedAtDesc(Long userId);

    Optional<VerificationSubmission> findByExternalReference(String externalReference);

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);
}
