package com.finance.backend.service;

import com.finance.backend.dto.*;
import com.finance.backend.exception.AppException;
import com.finance.backend.model.Role;
import com.finance.backend.model.User;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder
    private final JwtUtil jwtUtil;

    
    
    public LoginResponse login(LoginRequest request) {

       
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AppException(
                        "Invalid email or password.",  // Generic message (security)
                        HttpStatus.UNAUTHORIZED
                ));

        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }

        
        if ("INACTIVE".equals(user.getStatus())) {
            throw new AppException(
                    "Your account has been deactivated. Please contact an administrator.",
                    HttpStatus.FORBIDDEN
            );
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getRole().name(),
                user.getEmail()
        );

        // Return the token and user info (safe fields only — no password hash)
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    
    public UserDto register(RegisterRequest request) {

        // Step 1: Prevent duplicate usernames (case-insensitive)
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new AppException(
                    "Username '" + request.getUsername() + "' is already taken.",
                    HttpStatus.CONFLICT
            );
        }

        // Step 2: Prevent duplicate emails
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new AppException(
                    "Email '" + request.getEmail() + "' is already registered.",
                    HttpStatus.CONFLICT
            );
        }

        // Step 3: Hash the password using BCrypt
        // Never store plain-text passwords!
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Use VIEWER as default role if not specified in the request
        Role role = request.getRole() != null ? request.getRole() : Role.VIEWER;

        // Step 4: Build and save the new user
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase())  // Normalize email to lowercase
                .passwordHash(hashedPassword)
                .role(role)
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);

        // Convert to DTO (excludes passwordHash from the response)
        return UserDto.fromUser(savedUser);
    }

    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
        return UserDto.fromUser(user);
    }
}
