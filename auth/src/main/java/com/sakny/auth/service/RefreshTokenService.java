package com.sakny.auth.service;

import com.sakny.auth.entity.RefreshToken;
import com.sakny.auth.exception.AuthErrorCode;
import com.sakny.auth.repository.RefreshTokenRepository;
import com.sakny.common.exception.BusinessException;
import com.sakny.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        String familyId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .familyId(familyId)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshTokenResult rotateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        if (Boolean.TRUE.equals(existing.getRevoked())) {
            log.warn("Reuse detected for family {}, revoking all tokens for user {}",
                    existing.getFamilyId(), existing.getUser().getId());
            refreshTokenRepository.revokeAllByFamily(existing.getFamilyId());
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }

        if (existing.isExpired()) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        String newRawToken = generateSecureToken();
        String newHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(newHash)
                .user(existing.getUser())
                .familyId(existing.getFamilyId())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        refreshTokenRepository.save(newToken);

        return new RefreshTokenResult(existing.getUser(), newRawToken);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUser(userId);
    }

    @Transactional
    public void cleanupExpired() {
        int deleted = refreshTokenRepository.deleteExpired();
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    public record RefreshTokenResult(User user, String newRefreshToken) {}
}
