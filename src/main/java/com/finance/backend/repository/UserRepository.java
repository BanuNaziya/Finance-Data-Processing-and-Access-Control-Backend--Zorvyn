package com.finance.backend.repository;

import com.finance.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

   
    Optional<User> findByEmailIgnoreCase(String email);

   
    Optional<User> findByUsernameIgnoreCase(String username);

    
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Check if a user exists with the given username (case-insensitive).
     */
    boolean existsByUsernameIgnoreCase(String username);

    
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Check if another user (not the one being updated) has this username.
     */
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
}
