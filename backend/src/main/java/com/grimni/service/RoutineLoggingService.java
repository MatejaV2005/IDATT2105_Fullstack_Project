package com.grimni.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.PrerequisiteRoutineRecord;
import com.grimni.domain.User;
import com.grimni.domain.enums.ResultStatus;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.dto.AssignedRoutineResponse;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteRoutineRecordRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.RoutineUserBridgeRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service for managing the execution and logging of recurring prerequisite routines.
 * <p>
 * This service handles the operational lifecycle of routines, including:
 * <ul>
 * <li>Identifying routines assigned to specific users for execution.</li>
 * <li>Calculating active validity windows based on {@link IntervalRule} configurations.</li>
 * <li>Enforcing business constraints to prevent duplicate completions within the same interval.</li>
 * <li>Persisting completion records (logs) to maintain audit trails for compliance.</li>
 * </ul>
 */
@Service
public class RoutineLoggingService {

    private static final EnumSet<RoutineUserRole> MOBILE_EXECUTION_ROLES =
        EnumSet.of(RoutineUserRole.PERFORMER, RoutineUserRole.DEPUTY);

    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final PrerequisiteRoutineRepository prerequisiteRoutineRepository;
    private final RoutineUserBridgeRepository routineUserBridgeRepository;
    private final PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public RoutineLoggingService(
            OrgUserBridgeRepository orgUserBridgeRepository,
            PrerequisiteRoutineRepository prerequisiteRoutineRepository,
            RoutineUserBridgeRepository routineUserBridgeRepository,
            PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository,
            UserRepository userRepository) {
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.prerequisiteRoutineRepository = prerequisiteRoutineRepository;
        this.routineUserBridgeRepository = routineUserBridgeRepository;
        this.prerequisiteRoutineRecordRepository = prerequisiteRoutineRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all routines assigned to the authenticated user that are eligible for execution.
     * <p>
     * Returns routines where the user is either a Performer or a Deputy. Each routine
     * is enriched with the status of its current interval window and the latest historical log.
     *
     * @param authenticatedUserId the unique ID of the requesting user.
     * @param orgId the unique ID of the organization scope.
     * @return a sorted list of {@link AssignedRoutineResponse} objects.
     * @throws EntityNotFoundException if the user is not a member of the organization.
     */
    @Transactional(readOnly = true)
    public List<AssignedRoutineResponse> getAssignedRoutines(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        List<PrerequisiteRoutine> routines = prerequisiteRoutineRepository.findAssignedToUserWithDetails(
            authenticatedUserId,
            orgId,
            MOBILE_EXECUTION_ROLES
        );
        if (routines.isEmpty()) {
            return List.of();
        }

        Map<Long, List<PrerequisiteRoutineRecord>> recordsByRoutineId = prerequisiteRoutineRecordRepository
            .findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(
                orgId,
                routines.stream().map(PrerequisiteRoutine::getId).toList()
            )
            .stream()
            .collect(Collectors.groupingBy(record -> record.getRoutine().getId()));

        return routines.stream()
            .sorted(Comparator.comparing(PrerequisiteRoutine::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
            .map(routine -> toAssignedRoutineResponse(routine, recordsByRoutineId.getOrDefault(routine.getId(), List.of())))
            .toList();
    }

    /**
     * Records the completion of a specific routine for the current active interval.
     * <p>
     * This method performs strict validation to ensure:
     * <ul>
     * <li>The user is authorized to perform the routine.</li>
     * <li>The routine's interval window is currently active.</li>
     * <li>The routine has not already been completed for the current window.</li>
     * </ul>
     *
     * @param routineId the unique identifier of the routine to complete.
     * @param authenticatedUserId the unique ID of the user performing the action.
     * @param orgId the organization ID for scope validation.
     * @return the updated {@link AssignedRoutineResponse} reflecting the completion.
     * @throws EntityNotFoundException if the routine or user is not found.
     * @throws AccessDeniedException if the user does not have an execution role for this routine.
     * @throws IllegalArgumentException if the window is inactive or already completed.
     */
    @Transactional
    public AssignedRoutineResponse completeRoutine(Long routineId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        PrerequisiteRoutine routine = prerequisiteRoutineRepository.findByIdAndOrganization_Id(routineId, orgId)
            .orElseThrow(() -> new EntityNotFoundException("Prerequisite routine not found"));

        boolean assignedToRoutine = routineUserBridgeRepository.existsByUserAndRoutineAndRoles(
            authenticatedUserId,
            routineId,
            orgId,
            MOBILE_EXECUTION_ROLES
        );
        if (!assignedToRoutine) {
            throw new AccessDeniedException("You are not assigned to complete this routine");
        }

        IntervalWindow currentWindow = getCurrentWindow(routine.getIntervalRule());
        if (!currentWindow.active()) {
            throw new IllegalArgumentException("Routine cannot be completed before its interval starts");
        }

        boolean alreadyCompleted = prerequisiteRoutineRecordRepository
            .existsByOrganization_IdAndRoutine_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                orgId,
                routineId,
                currentWindow.start(),
                currentWindow.end()
            );
        if (alreadyCompleted) {
            throw new IllegalArgumentException("Routine is already completed for the current interval");
        }

        User performer = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        OrgUserBridge membership = ensureAuthenticatedMember(authenticatedUserId, orgId);

        PrerequisiteRoutineRecord record = new PrerequisiteRoutineRecord();
        record.setOrganization(membership.getOrganization());
        record.setRoutine(routine);
        record.setPerformedBy(performer);
        record.setResultStatus(ResultStatus.COMPLETED);
        prerequisiteRoutineRecordRepository.save(record);

        List<PrerequisiteRoutineRecord> records = prerequisiteRoutineRecordRepository
            .findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(orgId, List.of(routineId));

        return toAssignedRoutineResponse(routine, records);
    }

    /**
     * Verifies user membership within the organization.
     */
    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    /**
     * Maps a routine and its historical logs to an assigned routine response DTO.
     */
    private AssignedRoutineResponse toAssignedRoutineResponse(
            PrerequisiteRoutine routine,
            List<PrerequisiteRoutineRecord> records) {
        IntervalWindow currentWindow = getCurrentWindow(routine.getIntervalRule());
        PrerequisiteRoutineRecord currentIntervalRecord = currentWindow.active()
            ? records.stream()
                .filter(record -> !record.getCreatedAt().isBefore(currentWindow.start()) && record.getCreatedAt().isBefore(currentWindow.end()))
                .findFirst()
                .orElse(null)
            : null;
        PrerequisiteRoutineRecord latestRecord = records.stream().findFirst().orElse(null);

        return new AssignedRoutineResponse(
            routine.getId(),
            routine.getTitle(),
            routine.getPrerequisiteCategory() != null ? routine.getPrerequisiteCategory().getCategoryName() : null,
            routine.getDescription(),
            routine.getImmediateCorrectiveAction(),
            currentWindow.dueAt(),
            currentIntervalRecord != null,
            currentIntervalRecord != null ? currentIntervalRecord.getCreatedAt() : null,
            latestRecord != null ? latestRecord.getCreatedAt() : null
        );
    }

    /**
     * Calculates the chronological boundaries of the current routine interval.
     */
    private IntervalWindow getCurrentWindow(IntervalRule intervalRule) {
        if (intervalRule == null || intervalRule.getIntervalStart() == null || intervalRule.getIntervalRepeatTime() == null) {
            return new IntervalWindow(false, null, null, null);
        }

        long repeatSeconds = intervalRule.getIntervalRepeatTime();
        if (repeatSeconds <= 0) {
            return new IntervalWindow(false, null, null, null);
        }

        Instant now = Instant.now();
        Instant intervalStart = Instant.ofEpochSecond(intervalRule.getIntervalStart());
        if (now.isBefore(intervalStart)) {
            return new IntervalWindow(
                false,
                toLocalDateTime(intervalStart),
                toLocalDateTime(intervalStart.plusSeconds(repeatSeconds)),
                toLocalDateTime(intervalStart.plusSeconds(repeatSeconds))
            );
        }

        long elapsedSeconds = now.getEpochSecond() - intervalStart.getEpochSecond();
        long intervalIndex = elapsedSeconds / repeatSeconds;
        Instant windowStart = intervalStart.plusSeconds(intervalIndex * repeatSeconds);
        Instant windowEnd = windowStart.plusSeconds(repeatSeconds);

        return new IntervalWindow(
            true,
            toLocalDateTime(windowStart),
            toLocalDateTime(windowEnd),
            toLocalDateTime(windowEnd)
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Internal record representing the state and boundaries of a temporal interval window.
     */
    private record IntervalWindow(
        boolean active,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime dueAt
    ) {
    }
}