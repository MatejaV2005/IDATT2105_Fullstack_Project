package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

}
