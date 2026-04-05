package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
