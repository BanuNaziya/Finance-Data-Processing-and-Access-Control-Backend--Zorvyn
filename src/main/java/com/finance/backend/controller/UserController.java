package com.finance.backend.controller;

import com.finance.backend.dto.ApiResponse;
import com.finance.backend.dto.UserDto;
import com.finance.backend.dto.UserUpdateRequest;
import com.finance.backend.security.JwtUtil;
import com.finance.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserDto> users = userService.getAllUsers(role, status, search, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully.", users)
        );
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully.", user));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Extract the current admin's ID from their JWT token
        String token = authHeader.substring(7); // Remove "Bearer "
        Long currentUserId = jwtUtil.extractUserId(token);

        UserDto updatedUser = userService.updateUser(id, request, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully.", updatedUser)
        );
    }

  
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long currentUserId = jwtUtil.extractUserId(token);

        userService.deactivateUser(id, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success("User deactivated successfully.")
        );
    }
}
