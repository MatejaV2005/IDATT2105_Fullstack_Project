package com.grimni.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.enums.RoutineUserRole;

public interface PrerequisiteRoutineRepository extends JpaRepository<PrerequisiteRoutine, Long> {
    List<PrerequisiteRoutine> findByOrganization_Id(Long orgId);
    long countByOrganization_Id(Long orgId);
    Optional<PrerequisiteRoutine> findByIdAndOrganization_Id(Long id, Long orgId);
    boolean existsByIntervalRule_Id(Long intervalId);

    @Query("""
        select distinct routine
        from RoutineUserBridge bridge
        join bridge.routine routine
        left join fetch routine.prerequisiteCategory
        left join fetch routine.intervalRule
        where bridge.user.id = :userId
        and routine.organization.id = :orgId
        and bridge.id.userRole in :roles
        """)
    List<PrerequisiteRoutine> findAssignedToUserWithDetails(
        @Param("userId") Long userId,
        @Param("orgId") Long orgId,
        @Param("roles") Set<RoutineUserRole> roles
    );
}
