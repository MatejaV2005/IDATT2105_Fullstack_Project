package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.ids.OrgUserBridgeId;

public interface OrgUserBridgeRepository extends JpaRepository<OrgUserBridge, OrgUserBridgeId> {

}
