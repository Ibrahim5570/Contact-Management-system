package com.cms.backend.service;

import com.cms.backend.dto.LoginRequest;
import com.cms.backend.dto.RegisterRequest;
import com.cms.backend.dto.AuthResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Test")
                .email("ibrahim@test.com")
                .passwordHash("encodedPassword")
                .build();
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ibrahim");
        request.setLastName("Test");
        request.setEmail("ibrahim@test.com");
        request.setPassword("test123");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("ibrahim@test.com", response.getEmail());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("ibrahim@test.com");
        request.setPassword("test123");
        request.setFirstName("Ibrahim");
        request.setLastName("Test");

        when(userRepo.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Email already in use!", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ibrahim@test.com");
        request.setPassword("test123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("ibrahim@test.com", response.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@test.com");
        request.setPassword("test123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(request));
    }

    @Test
    void changePassword_ShouldSucceed_WhenCurrentPasswordCorrect() {
        com.cms.backend.dto.ChangePasswordRequest request =
                new com.cms.backend.dto.ChangePasswordRequest();
        request.setCurrentPassword("test123");
        request.setNewPassword("newpass123");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        assertDoesNotThrow(() ->
                authService.changePassword("ibrahim@test.com", request));

        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordWrong() {
        com.cms.backend.dto.ChangePasswordRequest request =
                new com.cms.backend.dto.ChangePasswordRequest();
        request.setCurrentPassword("wrongpass");
        request.setNewPassword("newpass123");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passEncoder.matches(anyString(), anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.changePassword("ibrahim@test.com", request));

        assertEquals("Current password is incorrect", exception.getMessage());
    }
}