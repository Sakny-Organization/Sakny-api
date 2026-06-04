package com.sakny.common.dto;

import com.sakny.common.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatusResponse {
    private Long submissionId;
    private VerificationStatus status;  // null = no submission ever made
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
    private Boolean isVerified;
}
