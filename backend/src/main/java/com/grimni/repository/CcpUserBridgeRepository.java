package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.ids.CcpUserBridgeId;

public interface CcpUserBridgeRepository extends JpaRepository<CcpUserBridge, CcpUserBridgeId> {

}
