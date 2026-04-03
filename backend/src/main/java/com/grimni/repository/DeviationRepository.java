package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Deviation;

public interface DeviationRepository extends CrudRepository<Deviation, Long> {
}
