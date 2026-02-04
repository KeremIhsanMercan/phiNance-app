package com.kerem.phinance.controller;

import com.kerem.phinance.dto.auth.*;
import com.kerem.phinance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PutMapping("/update-profile")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody com.kerem.phinance.dto.auth.UpdateProfileRequest request) {
        AuthResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    // Email verification endpoint removed
    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/delete-account")
    @Operation(summary = "Delete (deactivate) current user account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @RequestBody Map<String, String> request) {
        String password = request.get("password");
        authService.deleteAccount(password);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}
