package com.grimni.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.Ccp;
import com.grimni.domain.enums.RoutineUserRole;

public interface CcpRepository extends JpaRepository<Ccp, Long> {
    List<Ccp> findByOrganization_Id(Long orgId);
    Optional<Ccp> findByIdAndOrganization_Id(Long id, Long orgId);
    boolean existsByIntervalRule_Id(Long intervalId);

    @Query("""
        select distinct ccp
        from CcpUserBridge bridge
        join bridge.ccp ccp
        left join fetch ccp.intervalRule
        where bridge.user.id = :userId
        and ccp.organization.id = :orgId
        and bridge.id.userRole in :roles
        """)
    List<Ccp> findAssignedToUserWithDetails(
        @Param("userId") Long userId,
        @Param("orgId") Long orgId,
        @Param("roles") Set<RoutineUserRole> roles
    );
}
