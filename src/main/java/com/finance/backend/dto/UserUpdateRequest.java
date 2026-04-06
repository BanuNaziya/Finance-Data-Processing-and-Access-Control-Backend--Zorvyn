package com.finance.backend.dto;

import com.finance.backend.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequest {

    /** New username — if null, the current username is kept */
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain letters, numbers, and underscores"
    )
    private String username;

    /** New email — if null, current email is kept */
    @Email(message = "Please provide a valid email address")
    private String email;

    /** New password — will be hashed before saving. If null, password is unchanged */
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** New role — if null, role is unchanged */
    private Role role;

    
    @Pattern(
        regexp = "^(ACTIVE|INACTIVE)$",
        message = "Status must be either ACTIVE or INACTIVE"
    )
    private String status;
}
