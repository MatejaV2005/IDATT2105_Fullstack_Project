package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByOrganization_Id(Long orgId);
    long countByOrganization_Id(Long orgId);
    Optional<ProductCategory> findByIdAndOrganization_Id(Long id, Long orgId);
    List<ProductCategory> findByOrganization_IdAndIdIn(Long orgId, Collection<Long> ids);

    @Query("""
            SELECT DISTINCT pc
            FROM ProductCategory pc
            LEFT JOIN FETCH pc.dangerRiskCombos
            WHERE pc.organization.id = :orgId
            ORDER BY pc.id ASC
            """)
    List<ProductCategory> findByOrgIdFetchDangerRiskCombos(@Param("orgId") Long orgId);
}
