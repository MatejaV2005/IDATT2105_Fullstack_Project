package com.grimni.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.ids.CcpUserBridgeId;

public interface CcpUserBridgeRepository extends JpaRepository<CcpUserBridge, CcpUserBridgeId> {
    @Query("""
        select bridge
        from CcpUserBridge bridge
        join fetch bridge.user
        where bridge.ccp.id in :ccpIds
        """)
    List<CcpUserBridge> findAllWithUserByCcpIds(@Param("ccpIds") Collection<Long> ccpIds);

    void deleteByCcp_Id(Long ccpId);
}
