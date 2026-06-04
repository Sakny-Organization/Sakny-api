package com.sakny.auth.service;

import com.sakny.auth.config.TwilioProperties;
import com.sakny.auth.entity.OtpToken;
import com.sakny.auth.exception.AuthErrorCode;
import com.sakny.auth.repository.OtpTokenRepository;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.model.OtpChannel;
import com.sakny.common.model.OtpPurpose;
import com.sakny.user.entity.User;
import com.sakny.user.repository.UserRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final TwilioProperties twilioProperties;
    private final UserRepository userRepository;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public void sendOtp(User user, OtpChannel channel, OtpPurpose purpose) {
        otpTokenRepository.deleteByUserIdAndChannelAndPurpose(user.getId(), channel, purpose);

        String rawCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String codeHash = passwordEncoder.encode(rawCode);

        OtpToken token = OtpToken.builder()
                .user(user)
                .codeHash(codeHash)
                .channel(channel)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpTokenRepository.save(token);

        try {
            if (channel == OtpChannel.EMAIL) {
                sendEmail(user.getEmail(), rawCode, purpose);
            } else {
                sendSms(user.getPhone(), rawCode, purpose);
            }
        } catch (Exception e) {
            log.error("Failed to send OTP via {} to user {}: {}", channel, user.getId(), e.getMessage());
            throw new BusinessException(AuthErrorCode.OTP_SEND_FAILED, "Failed to send OTP: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyOtp(User user, String rawCode, OtpChannel channel, OtpPurpose purpose) {
        OtpToken token = otpTokenRepository
                .findTopByUserIdAndChannelAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), channel, purpose)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.OTP_NOT_FOUND));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(AuthErrorCode.OTP_EXPIRED);
        }

        if (!passwordEncoder.matches(rawCode, token.getCodeHash())) {
            throw new BusinessException(AuthErrorCode.OTP_INVALID);
        }

        token.setUsedAt(LocalDateTime.now());
        otpTokenRepository.save(token);

        // Mark contact channel as verified on the user
        if (purpose == OtpPurpose.REGISTRATION || purpose == OtpPurpose.PASSWORD_RESET) {
            if (channel == OtpChannel.EMAIL) {
                user.setIsEmailVerified(true);
            } else {
                user.setIsPhoneVerified(true);
            }
            userRepository.save(user);
        }
    }

    private void sendEmail(String email, String code, OtpPurpose purpose) throws Exception {
        String subject = purpose == OtpPurpose.REGISTRATION
                ? "Verify your Sakny account"
                : "Reset your Sakny password";

        String body = buildEmailBody(code, purpose);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        log.info("OTP email sent to {}", email);
    }

    private void sendSms(String phone, String code, OtpPurpose purpose) {
        Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
        String body = purpose == OtpPurpose.REGISTRATION
                ? "Your Sakny verification code is: " + code + ". It expires in 10 minutes."
                : "Your Sakny password reset code is: " + code + ". It expires in 10 minutes.";

        Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(twilioProperties.getFromNumber()),
                body
        ).create();
        log.info("OTP SMS sent to {}", phone);
    }

    private String buildEmailBody(String code, OtpPurpose purpose) {
        String actionText = purpose == OtpPurpose.REGISTRATION
                ? "verify your account"
                : "reset your password";
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                    <h2 style="color: #111;">Your Sakny verification code</h2>
                    <p style="color: #555;">Use the code below to %s. It expires in 10 minutes.</p>
                    <div style="background: #f4f4f4; border-radius: 8px; padding: 24px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #111;">%s</span>
                    </div>
                    <p style="color: #888; font-size: 13px;">If you didn't request this, you can safely ignore this email.</p>
                </div>
                """.formatted(actionText, code);
    }
}
