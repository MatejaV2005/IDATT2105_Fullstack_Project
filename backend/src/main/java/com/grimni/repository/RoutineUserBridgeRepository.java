package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.ids.RoutineUserBridgeId;

public interface RoutineUserBridgeRepository extends JpaRepository<RoutineUserBridge, RoutineUserBridgeId> {

}
