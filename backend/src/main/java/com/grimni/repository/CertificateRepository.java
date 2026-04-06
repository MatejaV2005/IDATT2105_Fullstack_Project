package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
}
