package com.grimni.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.Ccp;
import com.grimni.domain.CcpRecord;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.dto.AssignedCcpResponse;
import com.grimni.dto.CreateCcpRecordRequest;
import com.grimni.dto.SubmittedCcpRecordResponse;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.CcpUserBridgeRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service for managing the logging and retrieval of Critical Control Point (CCP) records.
 * <p>
 * This service handles the business logic for CCP monitoring, including:
 * <ul>
 * <li>Validating user authorization and organizational membership.</li>
 * <li>Calculating active monitoring windows based on recurring intervals.</li>
 * <li>Enforcing "one submission per interval" constraints.</li>
 * <li>Analyzing measured values against critical limits for food safety compliance.</li>
 * </ul>
 */
@Service
public class CcpLoggingService {

    private static final EnumSet<RoutineUserRole> MOBILE_EXECUTION_ROLES =
        EnumSet.of(RoutineUserRole.PERFORMER, RoutineUserRole.DEPUTY);
    private static final DateTimeFormatter REPEAT_TEXT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final CcpRepository ccpRepository;
    private final CcpUserBridgeRepository ccpUserBridgeRepository;
    private final CcpRecordRepository ccpRecordRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public CcpLoggingService(
            OrgUserBridgeRepository orgUserBridgeRepository,
            CcpRepository ccpRepository,
            CcpUserBridgeRepository ccpUserBridgeRepository,
            CcpRecordRepository ccpRecordRepository,
            UserRepository userRepository) {
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.ccpRepository = ccpRepository;
        this.ccpUserBridgeRepository = ccpUserBridgeRepository;
        this.ccpRecordRepository = ccpRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all CCPs assigned to the authenticated user within a specific organization.
     * <p>
     * Filters for CCPs where the user holds an execution role (Performer or Deputy) and 
     * enriches the response with information about current interval status and latest logs.
     *
     * @param authenticatedUserId the unique ID of the requesting user.
     * @param orgId the unique ID of the organization scope.
     * @return a sorted list of {@link AssignedCcpResponse} objects.
     * @throws EntityNotFoundException if the user-organization membership is not found.
     */
    @Transactional(readOnly = true)
    public List<AssignedCcpResponse> getAssignedCcps(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        List<Ccp> ccps = ccpRepository.findAssignedToUserWithDetails(
            authenticatedUserId,
            orgId,
            MOBILE_EXECUTION_ROLES
        );
        if (ccps.isEmpty()) {
            return List.of();
        }

        Map<Long, List<CcpRecord>> recordsByCcpId = ccpRecordRepository
            .findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(
                orgId,
                ccps.stream().map(Ccp::getId).toList()
            )
            .stream()
            .collect(Collectors.groupingBy(record -> record.getCcp().getId()));

        return ccps.stream()
            .sorted(Comparator.comparing(Ccp::getCcpName, Comparator.nullsLast(String::compareToIgnoreCase)))
            .map(ccp -> toAssignedCcpResponse(ccp, recordsByCcpId.getOrDefault(ccp.getId(), List.of())))
            .toList();
    }

    /**
     * Persists a new measurement record for a specific CCP.
     * <p>
     * Validates that the user is assigned to the CCP, that the interval window is currently active, 
     * and that no other record has been submitted for the same time window.
     *
     * @param ccpId unique identifier of the target CCP.
     * @param request the DTO containing measurement data and comments.
     * @param authenticatedUserId the unique ID of the performing user.
     * @param orgId the unique ID of the organization scope.
     * @return {@link SubmittedCcpRecordResponse} containing the saved record ID and limit analysis.
     * @throws EntityNotFoundException if the CCP or user does not exist.
     * @throws AccessDeniedException if the user is not authorized to log for this CCP.
     * @throws IllegalArgumentException if the window is inactive or the CCP is already completed for this interval.
     */
    @Transactional
    public SubmittedCcpRecordResponse createRecord(
            Long ccpId,
            CreateCcpRecordRequest request,
            Long authenticatedUserId,
            Long orgId) {
        OrgUserBridge membership = ensureAuthenticatedMember(authenticatedUserId, orgId);

        Ccp ccp = ccpRepository.findByIdAndOrganization_Id(ccpId, orgId)
            .orElseThrow(() -> new EntityNotFoundException("Critical control point not found"));

        boolean assignedToCcp = ccpUserBridgeRepository.existsByUserAndCcpAndRoles(
            authenticatedUserId,
            ccpId,
            orgId,
            MOBILE_EXECUTION_ROLES
        );
        if (!assignedToCcp) {
            throw new AccessDeniedException("You are not assigned to complete this critical control point");
        }

        IntervalWindow currentWindow = getCurrentWindow(ccp.getIntervalRule());
        if (!currentWindow.active()) {
            throw new IllegalArgumentException("Critical control point cannot be completed before its interval starts");
        }

        boolean alreadyCompleted = ccpRecordRepository
            .existsByOrganization_IdAndCcp_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                orgId,
                ccpId,
                currentWindow.start(),
                currentWindow.end()
            );
        if (alreadyCompleted) {
            throw new IllegalArgumentException("Critical control point is already completed for the current interval");
        }

        User performer = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CcpRecord record = new CcpRecord();
        record.setOrganization(membership.getOrganization());
        record.setCcp(ccp);
        record.setPerformedBy(performer);
        record.setComment(request.comment());
        record.setMeasuredValue(request.measuredValue());
        record.setCriticalMin(ccp.getCriticalMin());
        record.setCriticalMax(ccp.getCriticalMax());
        record.setUnit(ccp.getUnit());
        record.setCcpName(ccp.getCcpName());

        CcpRecord savedRecord = ccpRecordRepository.save(record);

        List<CcpRecord> records = ccpRecordRepository
            .findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(orgId, List.of(ccpId));

        return new SubmittedCcpRecordResponse(
            toAssignedCcpResponse(ccp, records),
            savedRecord.getId(),
            isOutsideCriticalRange(request.measuredValue(), ccp.getCriticalMin(), ccp.getCriticalMax())
        );
    }

    /**
     * Verifies that the user belongs to the specified organization.
     */
    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    /**
     * Maps CCP and its historical records to a response DTO for assigned users.
     */
    private AssignedCcpResponse toAssignedCcpResponse(Ccp ccp, List<CcpRecord> records) {
        IntervalWindow currentWindow = getCurrentWindow(ccp.getIntervalRule());
        CcpRecord currentIntervalRecord = currentWindow.active()
            ? records.stream()
                .filter(record -> !record.getCreatedAt().isBefore(currentWindow.start()) && record.getCreatedAt().isBefore(currentWindow.end()))
                .findFirst()
                .orElse(null)
            : null;
        CcpRecord latestRecord = records.stream().findFirst().orElse(null);

        return new AssignedCcpResponse(
            ccp.getId(),
            ccp.getCcpName(),
            ccp.getMonitoredDescription(),
            ccp.getCriticalMin(),
            ccp.getCriticalMax(),
            ccp.getUnit(),
            buildRepeatText(ccp.getIntervalRule()),
            currentWindow.dueAt(),
            currentIntervalRecord != null,
            currentIntervalRecord != null ? currentIntervalRecord.getCreatedAt() : null,
            latestRecord != null ? latestRecord.getCreatedAt() : null,
            latestRecord != null ? latestRecord.getMeasuredValue() : null,
            ccp.getImmediateCorrectiveAction()
        );
    }

    /**
     * Generates a human-readable description of the recurrence rule.
     */
    private String buildRepeatText(IntervalRule intervalRule) {
        if (intervalRule == null || intervalRule.getIntervalStart() == null || intervalRule.getIntervalRepeatTime() == null) {
            return null;
        }

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(intervalRule.getIntervalStart()), zoneId);
        return "Starts " + REPEAT_TEXT_FORMATTER.format(start)
            + ", repeats every " + humanizeDuration(intervalRule.getIntervalRepeatTime());
    }

    /**
     * Converts a duration in seconds to a human-readable string (weeks, days, hours, etc.).
     */
    private String humanizeDuration(Long seconds) {
        if (seconds == null) {
            return "unknown interval";
        }
        if (seconds % 604800 == 0) {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week" : " weeks");
        }
        if (seconds % 86400 == 0) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day" : " days");
        }
        if (seconds % 3600 == 0) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour" : " hours");
        }
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
        return seconds + (seconds == 1 ? " second" : " seconds");
    }

    /**
     * Checks if the measured value falls outside the defined critical safety range.
     */
    private boolean isOutsideCriticalRange(BigDecimal measuredValue, BigDecimal criticalMin, BigDecimal criticalMax) {
        if (measuredValue == null) {
            return false;
        }
        if (criticalMin != null && measuredValue.compareTo(criticalMin) < 0) {
            return true;
        }
        return criticalMax != null && measuredValue.compareTo(criticalMax) > 0;
    }

    /**
     * Calculates the current temporal window for a CCP based on its recurrence rule.
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

    /**
     * Utility to convert {@link Instant} to local time using the system default zone.
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Temporal metadata for an active or future interval window.
     */
    private record IntervalWindow(
        boolean active,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime dueAt
    ) {
    }
}