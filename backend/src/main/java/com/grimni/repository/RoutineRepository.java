package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Routine;

public interface RoutineRepository extends CrudRepository<Routine, Long> {
}
