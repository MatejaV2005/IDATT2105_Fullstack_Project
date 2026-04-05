package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
