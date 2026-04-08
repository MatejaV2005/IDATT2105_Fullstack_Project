package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Ccp;

public interface CcpRepository extends JpaRepository<Ccp, Long> {
    boolean existsByIntervalRule_Id(Long intervalId);
}
