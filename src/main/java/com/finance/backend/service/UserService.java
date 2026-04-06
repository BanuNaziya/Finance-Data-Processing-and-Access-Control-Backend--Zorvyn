package com.finance.backend.service;

import com.finance.backend.dto.UserDto;
import com.finance.backend.dto.UserUpdateRequest;
import com.finance.backend.exception.AppException;
import com.finance.backend.model.User;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

   
    public Page<UserDto> getAllUsers(String role, String status, String search, int page, int size) {

        // Build pageable with sorting by createdAt descending (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Start with a base specification that matches all users
        // We'll AND more conditions to it below
        Specification<User> spec = Specification.where(null);

        // Add role filter if provided
        if (role != null && !role.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    // root.get("role") accesses the 'role' field on the User entity
                    cb.equal(root.get("role").as(String.class), role.toUpperCase())
            );
        }

        // Add status filter if provided
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status.toUpperCase())
            );
        }

        // Add search filter (partial match on username OR email)
        if (search != null && !search.isBlank()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    // cb.or() combines two conditions with OR logic
                    cb.or(
                            cb.like(cb.lower(root.get("username")), searchPattern),
                            cb.like(cb.lower(root.get("email")), searchPattern)
                    )
            );
        }

        // Execute the query with all accumulated specifications
        // findAll(spec, pageable) is from JpaSpecificationExecutor
        Page<User> usersPage = userRepository.findAll(spec, pageable);

        // Map each User entity to a UserDto (removes passwordHash)
        return usersPage.map(UserDto::fromUser);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "User with ID " + id + " not found.", HttpStatus.NOT_FOUND
                ));
        return UserDto.fromUser(user);
    }

    
    public UserDto updateUser(Long targetId, UserUpdateRequest request, Long currentUserId) {

        // Find the user to update
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new AppException(
                        "User with ID " + targetId + " not found.", HttpStatus.NOT_FOUND
                ));

        // Business Rule: Admin cannot deactivate their own account
        if (targetId.equals(currentUserId) && "INACTIVE".equals(request.getStatus())) {
            throw new AppException(
                    "You cannot deactivate your own account.", HttpStatus.BAD_REQUEST
            );
        }

        // Check for duplicate username (if being changed)
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.getUsername(), targetId)) {
                throw new AppException(
                        "Username '" + request.getUsername() + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            user.setUsername(request.getUsername());
        }

        // Check for duplicate email (if being changed)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), targetId)) {
                throw new AppException(
                        "Email '" + request.getEmail() + "' is already registered.",
                        HttpStatus.CONFLICT
                );
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        // Update role if provided
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // Update status if provided
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus().toUpperCase());
        }

        // Re-hash and update password if a new one was provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Save the updated entity (Hibernate will generate UPDATE SQL)
        User updatedUser = userRepository.save(user);
        return UserDto.fromUser(updatedUser);
    }

    
    public void deactivateUser(Long targetId, Long currentUserId) {

        // Prevent self-deactivation
        if (targetId.equals(currentUserId)) {
            throw new AppException(
                    "You cannot deactivate your own account.", HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new AppException(
                        "User with ID " + targetId + " not found.", HttpStatus.NOT_FOUND
                ));

        // Idempotent check — don't error if already inactive
        if ("INACTIVE".equals(user.getStatus())) {
            throw new AppException("User is already inactive.", HttpStatus.BAD_REQUEST);
        }

        user.setStatus("INACTIVE");
        userRepository.save(user);
    }
}
