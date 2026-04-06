package com.finance.backend.dto;

import com.finance.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** The JWT token — client must include this in future requests */
    private String token;

    /** Always "Bearer" — tells the client how to use the token */
    @Builder.Default
    private String tokenType = "Bearer";

    /** User's unique ID */
    private Long id;

    /** User's display name */
    private String username;

    /** User's email */
    private String email;

    /** User's role — helps frontend show/hide features */
    private Role role;
}
