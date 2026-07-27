package com.cardpro.auth.service;

import com.cardpro.auth.dto.request.LoginRequest;
import com.cardpro.auth.dto.request.RegisterRequest;
import com.cardpro.auth.dto.response.AuthResponse;
import com.cardpro.auth.dto.response.UserResponse;
import com.cardpro.auth.entity.User;
import com.cardpro.auth.exception.InvalidCredentialsException;
import com.cardpro.auth.exception.UserAlreadyExistsException;
import com.cardpro.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .leadCredits(25)
            .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(
            user.getId().toString(),
            user.getEmail(),
            Collections.singletonList("USER")
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .user(mapToUserResponse(user))
            .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(
            user.getId().toString(),
            user.getEmail(),
            Collections.singletonList("USER")
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .user(mapToUserResponse(user))
            .build();
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .leadCredits(user.getLeadCredits())
            .build();
    }
}
