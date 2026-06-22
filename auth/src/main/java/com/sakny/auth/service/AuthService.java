package com.sakny.auth.service;

import com.sakny.auth.dto.*;
import com.sakny.auth.exception.AuthErrorCode;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.model.HousingRole;
import com.sakny.common.model.OtpChannel;
import com.sakny.common.model.OtpPurpose;
import com.sakny.common.model.Role;
import com.sakny.user.entity.User;
import com.sakny.user.repository.UserProfileRepository;
import com.sakny.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository repository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (repository.existsByPhone(request.getPhone())) {
            throw new BusinessException(AuthErrorCode.PHONE_ALREADY_EXISTS);
        }

        HousingRole housingRole = request.getHousingRole() != null
                ? request.getHousingRole()
                : HousingRole.ROOMMATE;

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .housingRole(housingRole)
                .build();
        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .housingRole(housingRole)
                .profileCompleted(false)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!user.isAccountNonLocked()) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        } catch (LockedException e) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        resetFailedAttempts(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        boolean profileCompleted = profileRepository.findByUserId(user.getId())
                .map(p -> Boolean.TRUE.equals(p.getIsComplete()))
                .orElse(false);

        return AuthenticationResponse.builder()
                .userId(user.getId())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .housingRole(user.getHousingRole())
                .profileCompleted(profileCompleted)
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenService.RefreshTokenResult result =
                refreshTokenService.rotateRefreshToken(request.getRefreshToken());

        User user = result.user();
        var jwtToken = jwtService.generateToken(user);

        boolean profileCompleted = profileRepository.findByUserId(user.getId())
                .map(p -> Boolean.TRUE.equals(p.getIsComplete()))
                .orElse(false);

        return AuthenticationResponse.builder()
                .userId(user.getId())
                .token(jwtToken)
                .refreshToken(result.newRefreshToken())
                .housingRole(user.getHousingRole())
                .profileCompleted(profileCompleted)
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }

    public void sendOtp(SendOtpRequest request) {
        User user = resolveUser(request.getIdentifier(), request.getChannel());
        otpService.sendOtp(user, request.getChannel(), request.getPurpose());
    }

    public void verifyOtp(VerifyOtpRequest request) {
        User user = resolveUser(request.getIdentifier(), request.getChannel());
        otpService.verifyOtp(user, request.getCode(), request.getChannel(), request.getPurpose());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = resolveUser(request.getIdentifier(), request.getChannel());
        otpService.verifyOtp(user, request.getCode(), request.getChannel(), OtpPurpose.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        refreshTokenService.revokeAllUserTokens(user.getId());
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        }

        repository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            repository.save(user);
        }
    }

    private User resolveUser(String identifier, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL) {
            return repository.findByEmail(identifier)
                    .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
        } else {
            return repository.findByPhone(identifier)
                    .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
        }
    }
}
