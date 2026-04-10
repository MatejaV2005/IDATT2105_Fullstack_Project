package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByOrganization_Id(Long orgId);
}
