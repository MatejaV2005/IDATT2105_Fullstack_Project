package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Ccp;

public interface CcpRepository extends JpaRepository<Ccp, Long> {
    List<Ccp> findByOrganization_Id(Long orgId);
    Optional<Ccp> findByIdAndOrganization_Id(Long id, Long orgId);
    boolean existsByIntervalRule_Id(Long intervalId);
}
