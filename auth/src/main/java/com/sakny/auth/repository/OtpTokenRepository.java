package com.sakny.auth.repository;

import com.sakny.auth.entity.OtpToken;
import com.sakny.common.model.OtpChannel;
import com.sakny.common.model.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByUserIdAndChannelAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            Long userId, OtpChannel channel, OtpPurpose purpose);

    void deleteByUserIdAndChannelAndPurpose(Long userId, OtpChannel channel, OtpPurpose purpose);
}
