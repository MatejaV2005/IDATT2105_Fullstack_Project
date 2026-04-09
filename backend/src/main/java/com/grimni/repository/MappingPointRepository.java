package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.MappingPoint;

public interface MappingPointRepository extends JpaRepository<MappingPoint, Long> {
    List<MappingPoint> findByOrganization_IdOrderByCreatedAtAscIdAsc(Long orgId);
    Optional<MappingPoint> findByIdAndOrganization_Id(Long id, Long orgId);
}
