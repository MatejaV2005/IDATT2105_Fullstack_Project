package com.grimni.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Certificate;

public interface CertificateRepository extends CrudRepository<Certificate, Long> {
    List<Certificate> findByUserUserId(Long userId);
}
