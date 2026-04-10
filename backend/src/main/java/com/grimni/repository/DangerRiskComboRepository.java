package com.grimni.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.DangerRiskCombo;

public interface DangerRiskComboRepository extends JpaRepository<DangerRiskCombo, Long> {

    Optional<DangerRiskCombo> findByIdAndProductCategory_Organization_Id(Long id, Long orgId);
}
