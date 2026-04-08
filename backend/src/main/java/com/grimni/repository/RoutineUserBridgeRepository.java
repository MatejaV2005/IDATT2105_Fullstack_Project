package com.grimni.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.ids.RoutineUserBridgeId;

public interface RoutineUserBridgeRepository extends JpaRepository<RoutineUserBridge, RoutineUserBridgeId> {
    @Query("""
        select bridge
        from RoutineUserBridge bridge
        join fetch bridge.user
        where bridge.routine.id in :routineIds
        """)
    List<RoutineUserBridge> findAllWithUserByRoutineIds(@Param("routineIds") Collection<Long> routineIds);

    void deleteByRoutine_Id(Long routineId);
}
