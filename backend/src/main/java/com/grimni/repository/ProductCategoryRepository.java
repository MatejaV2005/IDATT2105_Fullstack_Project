package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByOrganization_Id(Long orgId);
    Optional<ProductCategory> findByIdAndOrganization_Id(Long id, Long orgId);
    List<ProductCategory> findByOrganization_IdAndIdIn(Long orgId, Collection<Long> ids);
}
