package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.PrerequisiteCategory;

public interface PrerequisiteCategoryRepository extends JpaRepository<PrerequisiteCategory, Long>  {
    List<PrerequisiteCategory> findByOrganization_Id(Long orgId);
    Optional<PrerequisiteCategory> findByIdAndOrganization_Id(Long id, Long orgId);
}
