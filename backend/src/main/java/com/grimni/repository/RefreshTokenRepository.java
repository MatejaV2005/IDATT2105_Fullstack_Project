package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Todo;

public interface RefreshTokenRepository extends CrudRepository<Todo, Long> {

}
