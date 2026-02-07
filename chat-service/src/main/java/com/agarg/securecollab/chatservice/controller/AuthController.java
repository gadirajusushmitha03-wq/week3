package com.agarg.securecollab.chatservice.controller;

import com.agarg.securecollab.chatservice.security.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtTokenService jwtService;

    public AuthController(JwtTokenService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String username = request.get("username");
        String password = request.get("password"); // In production: validate against secure auth system

        // TODO: Validate credentials against database or external auth provider

        String token = jwtService.generateToken(userId, username, List.of("ROLE_USER", "ROLE_CHAT_USER"));

        return ResponseEntity.ok(Map.of(
            "token", token,
            "userId", userId,
            "username", username,
            "expiresIn", 3600
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.status(401).body("Invalid token");
            }
            String userId = jwtService.extractUserId(token);
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        String userId = jwtService.extractUserId(token);
        String newToken = jwtService.generateToken(userId, userId, List.of("ROLE_USER"));
        return ResponseEntity.ok(Map.of("token", newToken));
    }
}
