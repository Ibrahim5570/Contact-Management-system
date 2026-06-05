package com.cms.backend.service;

import com.cms.backend.dto.*;
import com.cms.backend.entity.User;
import com.cms.backend.repository.UserRepository;
import com.cms.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServicePhoneTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User phoneOnlyUser;

    @BeforeEach
    void setUp() {
        phoneOnlyUser = User.builder()
                .id(2L)
                .firstName("Phone")
                .lastName("User")
                .phoneNumber("03001234567")
                .passwordHash("encodedPassword")
                .build();
    }

    @Test
    void register_ShouldSucceed_WhenOnlyPhoneProvided() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Phone");
        request.setLastName("User");
        request.setPhoneNumber("03001234567");
        request.setPassword("test123");

        when(userRepo.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(phoneOnlyUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenNeitherEmailNorPhoneProvided() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("No");
        request.setLastName("Identifier");
        request.setPassword("test123");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Either email or phone number is required",
                exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenPhoneAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Phone");
        request.setLastName("User");
        request.setPhoneNumber("03001234567");
        request.setPassword("test123");

        when(userRepo.existsByPhoneNumber(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Phone number already in use!", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void login_ShouldSucceed_WithPhoneIdentifier() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("03001234567");
        request.setPassword("test123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepo.findByPhoneNumber(anyString()))
                .thenReturn(Optional.of(phoneOnlyUser));
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }

    @Test
    void register_ShouldSucceed_WhenBothEmailAndPhoneProvided() {
        User bothUser = User.builder()
                .id(3L).firstName("Both").lastName("User")
                .email("both@test.com").phoneNumber("03009876543")
                .passwordHash("encodedPassword").build();

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Both");
        request.setLastName("User");
        request.setEmail("both@test.com");
        request.setPhoneNumber("03009876543");
        request.setPassword("test123");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(userRepo.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(bothUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }
}