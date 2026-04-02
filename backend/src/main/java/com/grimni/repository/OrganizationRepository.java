package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Organization;

public interface OrganizationRepository extends CrudRepository<Organization, Long> {
}
