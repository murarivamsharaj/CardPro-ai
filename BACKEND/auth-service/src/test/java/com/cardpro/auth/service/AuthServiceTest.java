package com.cardpro.auth.service;

import com.cardpro.auth.dto.request.RegisterRequest;
import com.cardpro.auth.entity.User;
import com.cardpro.auth.exception.PasswordMismatchException;
import com.cardpro.auth.exception.RegistrationDisabledException;
import com.cardpro.auth.exception.UserAlreadyExistsException;
import com.cardpro.auth.repository.RefreshTokenRepository;
import com.cardpro.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RegistrationConfigService registrationConfigService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(registrationConfigService.isRegistrationEnabled()).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

        // Verify it never reached the save method
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenRegistrationDisabled() {
        // Arrange
        when(registrationConfigService.isRegistrationEnabled()).thenReturn(false);

        // Act & Assert
        assertThrows(RegistrationDisabledException.class, () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordIsWrong() {
        // Arrange
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("$2a$10$storedHash")
            .build();
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "$2a$10$storedHash")).thenReturn(false);

        // Act & Assert
        assertThrows(
            PasswordMismatchException.class,
            () -> authService.changePassword(user.getId(), "wrong-password", "new-password-123")
        );

        // The password must never be re-encoded or saved on a mismatch
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldEncodeAndSave_WhenCurrentPasswordMatches() {
        // Arrange
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("$2a$10$storedHash")
            .build();
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "$2a$10$storedHash")).thenReturn(true);
        when(passwordEncoder.encode("new-password-123")).thenReturn("$2a$10$freshHash");

        // Act
        authService.changePassword(user.getId(), "current-password", "new-password-123");

        // Assert
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllUserTokens(user.getId());
    }
}
