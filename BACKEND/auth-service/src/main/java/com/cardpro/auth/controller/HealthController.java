package com.cardpro.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/auth/health")
    public ResponseEntity<String> keepAwake() {
        return ResponseEntity.ok("Server is awake and ready.");
    }
}