package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.RoutineUserBridgeId;

public interface RoutineUserBridgeRepository extends JpaRepository<RoutineUserBridge, RoutineUserBridgeId> {
    @Query("""
        select bridge
        from RoutineUserBridge bridge
        join fetch bridge.user
        where bridge.routine.id in :routineIds
        """)
    List<RoutineUserBridge> findAllWithUserByRoutineIds(@Param("routineIds") Collection<Long> routineIds);

    @Query("""
        select count(bridge) > 0
        from RoutineUserBridge bridge
        where bridge.user.id = :userId
        and bridge.routine.id = :routineId
        and bridge.routine.organization.id = :orgId
        and bridge.id.userRole in :roles
        """)
    boolean existsByUserAndRoutineAndRoles(
        @Param("userId") Long userId,
        @Param("routineId") Long routineId,
        @Param("orgId") Long orgId,
        @Param("roles") Set<RoutineUserRole> roles
    );

    @Query("""
        select bridge
        from RoutineUserBridge bridge
        join fetch bridge.user
        join fetch bridge.routine routine
        where routine.organization.id = :orgId
        """)
    List<RoutineUserBridge> findAllByOrganizationIdWithUser(@Param("orgId") Long orgId);

    void deleteByRoutine_Id(Long routineId);

    void deleteByRoutine_IdAndId_UserRole(Long routineId, RoutineUserRole userRole);
}
