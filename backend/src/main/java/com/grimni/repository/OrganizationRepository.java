package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grimni.domain.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
