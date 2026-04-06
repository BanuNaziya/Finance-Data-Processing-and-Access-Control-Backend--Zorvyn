package com.finance.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data  // Lombok: generates getters, setters, toString, equals, hashCode
public class LoginRequest {

    /** User's email address (used as the login identifier) */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /** Plain-text password (will be compared against stored bcrypt hash) */
    @NotBlank(message = "Password is required")
    private String password;
}
