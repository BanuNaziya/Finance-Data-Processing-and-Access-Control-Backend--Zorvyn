package com.finance.backend.config;

import com.finance.backend.security.JwtAuthFilter;
import com.finance.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity  
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
           
            .csrf(AbstractHttpConfigurer::disable)

            // ── URL Authorization Rules ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no authentication needed
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/health").permitAll()

                // H2 console (development only — remove in production)
                .requestMatchers("/h2-console/**").permitAll()

                // User management — admin only
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Transaction creation/update — admin and analyst
                .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasAnyRole("ADMIN", "ANALYST")
                .requestMatchers(HttpMethod.PUT,  "/api/transactions/**").hasAnyRole("ADMIN", "ANALYST")

                // Transaction deletion — admin only
                .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").hasRole("ADMIN")

                // Admin registration — admin only
                .requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")

                // All other requests — any authenticated user (viewer, analyst, admin)
                .anyRequest().authenticated()
            )

            
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Authentication Provider ────────────────────────────────────────
            .authenticationProvider(authenticationProvider())

           
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
