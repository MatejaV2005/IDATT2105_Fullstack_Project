package com.grimni.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.CcpRecord;

public interface CcpRecordRepository extends JpaRepository<CcpRecord, Long>  {

    @Query(value = """
        SELECT COUNT(*) FROM ccp_record cr
        WHERE cr.org_id = :orgId
          AND cr.verification_status = 'WAITING'
          AND (
              (cr.ccp_id IS NOT NULL AND EXISTS (
                  SELECT 1 FROM ccp_user_bridge cub
                  WHERE cub.ccp_id = cr.ccp_id
                    AND cub.user_id = :userId
                    AND cub.user_role = 'VERIFIER'
              ))
              OR
              (cr.ccp_id IS NULL AND :isManagerOrOwner = true)
          )
        """, nativeQuery = true)
    long countWaitingVerifications(
            @Param("orgId") Long orgId,
            @Param("userId") Long userId,
            @Param("isManagerOrOwner") boolean isManagerOrOwner);

    boolean existsByOrganization_IdAndCcp_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        Long orgId,
        Long ccpId,
        LocalDateTime createdAtGreaterThanEqual,
        LocalDateTime createdAtLessThan
    );

    List<CcpRecord> findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(
        Long orgId,
        Collection<Long> ccpIds
    );

}
