package com.cms.backend.service;

import com.cms.backend.dto.*;
import com.cms.backend.entity.User;
import com.cms.backend.repository.UserRepository;
import com.cms.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email/phone: {} / {}",
                request.getEmail(), request.getPhoneNumber());

        // Validate that at least one identifier is provided
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean hasPhone = request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new RuntimeException("Either email or phone number is required");
        }

        // Check for duplicates
        if (hasEmail && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use!");
        }
        if (hasPhone && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use!");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(hasEmail ? request.getEmail() : null)
                .phoneNumber(hasPhone ? request.getPhoneNumber() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        logger.info("User registered successfully!");

        // Use email if available, otherwise phone as the JWT subject
        String identifier = hasEmail ? user.getEmail() : user.getPhoneNumber();
        String token = jwtUtil.generateToken(identifier);
        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public AuthResponse login(LoginRequest request)
    {
        logger.info("Login attempt for: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("Logged in successfully: {}", request.getEmail());
        return new AuthResponse(token, user.getEmail(),
                user.getFirstName(), user.getLastName());
    }

    public void changePassword(String email, ChangePasswordRequest request)
    {
        logger.info("Password change request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
        {
            logger.warn("Password change failed! Incorrect current password for: {}", email);
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", email);
    }
}