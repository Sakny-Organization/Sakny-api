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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository repository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Starting registration for email: {}", request.getEmail());

        if (repository.existsByEmail(request.getEmail())) {
            log.error("Registration failed: Email {} is already taken", request.getEmail());
            throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (repository.existsByPhone(request.getPhone())) {
            log.error("Registration failed: Phone {} is already taken", request.getPhone());
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
        log.info("Saving user to repository...");
        repository.save(user);
        log.info("User saved successfully, generating token...");
        var jwtToken = jwtService.generateToken(user);
        log.info("Token generated successfully");
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .housingRole(housingRole)
                .profileCompleted(false)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        boolean profileCompleted = profileRepository.findByUserId(user.getId())
                .map(p -> Boolean.TRUE.equals(p.getIsComplete()))
                .orElse(false);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .housingRole(user.getHousingRole())
                .profileCompleted(profileCompleted)
                .build();
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
