package com.finance.backend.controller;

import com.finance.backend.dto.*;
import com.finance.backend.security.JwtUtil;
import com.finance.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    
    @PostMapping("/login")
    
    public ResponseEntity<ApiResponse<LoginResponse>> login(

            @Valid @RequestBody LoginRequest request) {

        
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful.", response)
        );
    }

  
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserDto newUser = authService.register(request);

        // HTTP 201 Created is the correct status for resource creation
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully.", newUser));
    }

    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from "Bearer <token>"
        String token = authHeader.substring(7);

        // Get the user ID from the JWT token
        Long userId = jwtUtil.extractUserId(token);

        UserDto profile = authService.getProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully.", profile)
        );
    }
}
