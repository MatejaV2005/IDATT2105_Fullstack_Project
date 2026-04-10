package com.grimni.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.ids.OrgUserBridgeId;

public interface OrgUserBridgeRepository extends JpaRepository<OrgUserBridge, OrgUserBridgeId> {
    List<OrgUserBridge> findByOrganizationId(Long orgId);
    List<OrgUserBridge> findByUserId(Long userId);
    Optional<OrgUserBridge> findByOrganizationIdAndUserId(Long orgId, Long userId);
    List<OrgUserBridge> findByOrganizationIdAndUserIdIn(Long orgId, Collection<Long> userIds);
    Optional<OrgUserBridge> findFirstByUserId(Long userId);
}
