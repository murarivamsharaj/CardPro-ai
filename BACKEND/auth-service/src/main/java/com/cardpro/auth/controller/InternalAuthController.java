package com.cardpro.auth.controller;

import com.cardpro.auth.dto.response.UserResponse;
import com.cardpro.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @GetMapping("/users/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserByEmail(email));
    }
}
