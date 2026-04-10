package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserId(Long userId);
    List<Certificate> findByOrganizationId(Long orgId);
    List<Certificate> findByUserIdAndOrganizationId(Long userId, Long orgId);
}
