package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Todo;

public interface MappingPointRepository extends CrudRepository<Todo, Long> {

}
