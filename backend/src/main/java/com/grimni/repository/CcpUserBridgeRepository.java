package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.CcpUserBridgeId;

public interface CcpUserBridgeRepository extends JpaRepository<CcpUserBridge, CcpUserBridgeId> {
    @Query("""
        select bridge
        from CcpUserBridge bridge
        join fetch bridge.user
        where bridge.ccp.id in :ccpIds
        """)
    List<CcpUserBridge> findAllWithUserByCcpIds(@Param("ccpIds") Collection<Long> ccpIds);

    @Query("""
        select count(bridge) > 0
        from CcpUserBridge bridge
        where bridge.user.id = :userId
        and bridge.ccp.id = :ccpId
        and bridge.ccp.organization.id = :orgId
        and bridge.id.userRole in :roles
        """)
    boolean existsByUserAndCcpAndRoles(
        @Param("userId") Long userId,
        @Param("ccpId") Long ccpId,
        @Param("orgId") Long orgId,
        @Param("roles") Set<RoutineUserRole> roles
    );

    @Query("""
        select bridge
        from CcpUserBridge bridge
        join fetch bridge.user
        join fetch bridge.ccp ccp
        where ccp.organization.id = :orgId
        """)
    List<CcpUserBridge> findAllByOrganizationIdWithUser(@Param("orgId") Long orgId);

    void deleteByCcp_Id(Long ccpId);
}
