package com.sakny.user.entity;

import com.sakny.common.model.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "front_id_url", nullable = false, length = 500)
    private String frontIdUrl;

    @Column(name = "back_id_url", nullable = false, length = 500)
    private String backIdUrl;

    @Column(name = "selfie_url", nullable = false, length = 500)
    private String selfieUrl;

    @Column(name = "external_reference", nullable = false, unique = true)
    private String externalReference;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
