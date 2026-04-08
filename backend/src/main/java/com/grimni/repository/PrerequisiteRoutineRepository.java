package com.grimni.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.PrerequisiteRoutine;

public interface PrerequisiteRoutineRepository extends JpaRepository<PrerequisiteRoutine, Long> {
    List<PrerequisiteRoutine> findByOrganization_Id(Long orgId);
    Optional<PrerequisiteRoutine> findByIdAndOrganization_Id(Long id, Long orgId);
    boolean existsByIntervalRule_Id(Long intervalId);
}
