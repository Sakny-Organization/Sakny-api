package com.sakny.auth.service;

import com.sakny.auth.dto.AuthenticationRequest;
import com.sakny.auth.dto.AuthenticationResponse;
import com.sakny.auth.dto.RegisterRequest;
import com.sakny.common.model.Role;
import com.sakny.user.entity.User;
import com.sakny.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationResponse register(RegisterRequest request) {
                log.info("Starting registration for email: {}", request.getEmail());

                if (repository.existsByEmail(request.getEmail())) {
                        log.error("Registration failed: Email {} is already taken", request.getEmail());
                        throw new RuntimeException("Email is already taken");
                }

                if (repository.existsByPhone(request.getPhone())) {
                        log.error("Registration failed: Phone {} is already taken", request.getPhone());
                        throw new RuntimeException("Phone number is already taken");
                }

                var user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.USER)
                                .build();
                log.info("Saving user to repository...");
                repository.save(user);
                log.info("User saved successfully, generating token...");
                var jwtToken = jwtService.generateToken(user);
                log.info("Token generated successfully");
                return AuthenticationResponse.builder()
                                .token(jwtToken)
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
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }
}
