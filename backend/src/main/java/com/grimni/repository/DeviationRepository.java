package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Deviation;

public interface DeviationRepository extends JpaRepository<Deviation, Long> {
}
