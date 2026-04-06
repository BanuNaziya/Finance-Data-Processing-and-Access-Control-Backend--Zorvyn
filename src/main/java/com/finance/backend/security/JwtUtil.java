package com.finance.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Token expiration time in milliseconds (from application.properties) */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    
    public String generateToken(Long userId, String role, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // "sub" claim = user ID
                .claim("role", role)               // Custom claim: user's role
                .claim("email", email)             // Custom claim: user's email
                .issuedAt(now)                     // "iat" claim = issued at
                .expiration(expiryDate)            // "exp" claim = expiry time
                .signWith(getSigningKey())         // Sign with HMAC-SHA256
                .compact();                        // Build the token string
    }

   
    public Long extractUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.parseLong(subject);
    }
    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // Will throw if invalid/expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException covers: ExpiredJwtException, MalformedJwtException,
            // SignatureException, UnsupportedJwtException
            return false;
        }
    }

    
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Set the key to verify signature
                .build()
                .parseSignedClaims(token)    // Parse + verify the token
                .getPayload();               // Get the claims payload
    }
}
