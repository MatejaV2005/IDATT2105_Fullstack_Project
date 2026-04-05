package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLegalName(String legalName);
    Optional<User> findByUsername(String username); 
    Optional<User> findByEmail(String email); 
    
}
