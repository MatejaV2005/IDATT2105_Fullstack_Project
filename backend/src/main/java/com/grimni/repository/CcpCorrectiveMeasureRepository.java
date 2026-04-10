package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.CcpCorrectiveMeasure;

public interface CcpCorrectiveMeasureRepository extends JpaRepository<CcpCorrectiveMeasure, Long> {
    @Query("""
        select measure
        from CcpCorrectiveMeasure measure
        join fetch measure.productCategory
        where measure.ccp.id in :ccpIds
        """)
    List<CcpCorrectiveMeasure> findAllWithProductCategoryByCcpIds(@Param("ccpIds") Collection<Long> ccpIds);

    Optional<CcpCorrectiveMeasure> findByIdAndCcp_Organization_Id(Long id, Long orgId);
    void deleteByCcp_Id(Long ccpId);
}
