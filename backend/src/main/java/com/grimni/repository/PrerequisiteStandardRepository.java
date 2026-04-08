package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.PrerequisiteStandard;

public interface PrerequisiteStandardRepository extends JpaRepository<PrerequisiteStandard, Long> {
    List<PrerequisiteStandard> findByPrerequisiteCategory_Organization_Id(Long orgId);
    Optional<PrerequisiteStandard> findByIdAndPrerequisiteCategory_Organization_Id(Long id, Long orgId);
}
