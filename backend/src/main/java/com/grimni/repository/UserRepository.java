package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
