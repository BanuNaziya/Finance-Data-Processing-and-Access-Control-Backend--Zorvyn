package com.finance.backend.security;

import com.finance.backend.model.User;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /** Injected by Spring via the generated constructor */
    private final UserRepository userRepository;

    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user in database by email
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

       
        String roleName = "ROLE_" + user.getRole().name(); // e.g., "ROLE_ADMIN"

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled("INACTIVE".equals(user.getStatus()))  // true = cannot log in
                .authorities(List.of(new SimpleGrantedAuthority(roleName)))
                .build();
    }
}
