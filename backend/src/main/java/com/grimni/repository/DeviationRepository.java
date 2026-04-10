package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.Deviation;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;

public interface DeviationRepository extends JpaRepository<Deviation, Long> {
    long countByOrganization_IdAndCategoryAndReviewStatus(Long orgId, DeviationCategory category, ReviewStatus reviewStatus);


    @Query(value = """
        SELECT * FROM deviation d
        WHERE d.org_id = :orgId
          AND (
              (d.ccp_record_id IS NOT NULL AND EXISTS (
                  SELECT 1 FROM ccp_record cr
                  JOIN ccp_user_bridge cub ON cub.ccp_id = cr.ccp_id
                  WHERE cr.id = d.ccp_record_id
                    AND cub.user_id = :userId
                    AND cub.user_role = 'DEVIATION_RECEIVER'
              ))
              OR
              (d.routine_record_id IS NOT NULL AND EXISTS (
                  SELECT 1 FROM prerequisite_routine_record prr
                  JOIN routine_user_bridge rub ON rub.routine_id = prr.routine_id
                  WHERE prr.id = d.routine_record_id
                    AND rub.user_id = :userId
                    AND rub.user_role = 'DEVIATION_RECEIVER'
              ))
              OR
              (d.ccp_record_id IS NULL AND d.routine_record_id IS NULL AND :isManagerOrOwner = true)
          )
        ORDER BY d.created_at DESC
        """, nativeQuery = true)
    List<Deviation> findReceivedDeviations(
            @Param("orgId") Long orgId,
            @Param("userId") Long userId,
            @Param("isManagerOrOwner") boolean isManagerOrOwner);

    @Query(value = """
        SELECT COUNT(*) FROM deviation d
        WHERE d.org_id = :orgId
          AND d.review_status = 'OPEN'
          AND (
              (d.ccp_record_id IS NOT NULL AND EXISTS (
                  SELECT 1 FROM ccp_record cr
                  JOIN ccp_user_bridge cub ON cub.ccp_id = cr.ccp_id
                  WHERE cr.id = d.ccp_record_id
                    AND cub.user_id = :userId
                    AND cub.user_role = 'DEVIATION_RECEIVER'
              ))
              OR
              (d.routine_record_id IS NOT NULL AND EXISTS (
                  SELECT 1 FROM prerequisite_routine_record prr
                  JOIN routine_user_bridge rub ON rub.routine_id = prr.routine_id
                  WHERE prr.id = d.routine_record_id
                    AND rub.user_id = :userId
                    AND rub.user_role = 'DEVIATION_RECEIVER'
              ))
              OR
              (d.ccp_record_id IS NULL AND d.routine_record_id IS NULL AND :isManagerOrOwner = true)
          )
        """, nativeQuery = true)
    long countOpenDeviationReviews(
            @Param("orgId") Long orgId,
            @Param("userId") Long userId,
            @Param("isManagerOrOwner") boolean isManagerOrOwner);

    @Query(value = """
        SELECT cub.user_id AS user_id, COUNT(DISTINCT d.id) AS open_count
        FROM deviation d
        JOIN ccp_record cr ON cr.id = d.ccp_record_id
        JOIN ccp_user_bridge cub ON cub.ccp_id = cr.ccp_id
        WHERE d.org_id = :orgId
          AND d.review_status = 'OPEN'
          AND cub.user_role = 'DEVIATION_RECEIVER'
        GROUP BY cub.user_id
        """, nativeQuery = true)
    List<Object[]> countOpenCcpDeviationReviewsByReceiver(@Param("orgId") Long orgId);

    @Query(value = """
        SELECT rub.user_id AS user_id, COUNT(DISTINCT d.id) AS open_count
        FROM deviation d
        JOIN prerequisite_routine_record prr ON prr.id = d.routine_record_id
        JOIN routine_user_bridge rub ON rub.routine_id = prr.routine_id
        WHERE d.org_id = :orgId
          AND d.review_status = 'OPEN'
          AND rub.user_role = 'DEVIATION_RECEIVER'
        GROUP BY rub.user_id
        """, nativeQuery = true)
    List<Object[]> countOpenRoutineDeviationReviewsByReceiver(@Param("orgId") Long orgId);

    List<Deviation> findByOrganization_Id(Long orgId);

}
