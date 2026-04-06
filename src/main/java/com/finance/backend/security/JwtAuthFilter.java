package com.finance.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

     
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Extract the JWT token from the Authorization header
        String token = extractTokenFromRequest(request);

        // Step 2: If there's no token, skip JWT processing entirely.
        // The SecurityConfig will block the request if the route needs auth.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Validate token (check signature and expiration)
        if (!jwtUtil.isTokenValid(token)) {
            // Token is expired or tampered — don't authenticate
            // The request will be rejected by Spring Security if auth is required
            filterChain.doFilter(request, response);
            return;
        }

        // Step 4: Only set authentication if not already authenticated
        // (prevents re-processing if another filter already set it)
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // Extract the email from the token's claims
            String email = jwtUtil.extractEmail(token);

            // Step 5: Load full user details from DB to get roles and status
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // If user is disabled (INACTIVE status), don't authenticate
            if (!userDetails.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }
// Step 6: Create an Authentication object and set it in the SecurityContext.
            
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Attach request details (IP address, session ID) to the auth token
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Store in SecurityContext — this makes the user "authenticated" for this request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Step 7: Continue the filter chain (pass to next filter or controller)
        filterChain.doFilter(request, response);
    }

    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Check the header exists and starts with "Bearer "
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix (7 chars)
        }

        return null; // No valid Authorization header
    }
}
