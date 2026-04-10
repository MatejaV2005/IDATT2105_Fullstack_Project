package com.grimni.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.PrerequisiteRoutineRecord;
import com.grimni.domain.enums.ResultStatus;

public interface PrerequisiteRoutineRecordRepository extends JpaRepository<PrerequisiteRoutineRecord, Long> {
    long countByOrganization_IdAndResultStatus(Long orgId, ResultStatus resultStatus);

    boolean existsByOrganization_IdAndRoutine_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        Long orgId,
        Long routineId,
        LocalDateTime createdAtGreaterThanEqual,
        LocalDateTime createdAtLessThan
    );

    List<PrerequisiteRoutineRecord> findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(
        Long orgId,
        Collection<Long> routineIds
    );
}
