package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.PrerequisiteCategory;
import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.PrerequisiteRoutineRecord;
import com.grimni.domain.User;
import com.grimni.domain.enums.ResultStatus;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.AssignedRoutineResponse;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteRoutineRecordRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.RoutineUserBridgeRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.RoutineLoggingService;

@ExtendWith(MockitoExtension.class)
public class RoutineLoggingServiceTest {

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private PrerequisiteRoutineRepository prerequisiteRoutineRepository;

    @Mock
    private RoutineUserBridgeRepository routineUserBridgeRepository;

    @Mock
    private PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoutineLoggingService routineLoggingService;

    private Organization organization;
    private User authenticatedUser;
    private PrerequisiteRoutine routine;
    private IntervalRule intervalRule;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", 10L);
        organization.setOrgName("Test Org");

        authenticatedUser = new User();
        ReflectionTestUtils.setField(authenticatedUser, "id", 99L);
        authenticatedUser.setLegalName("Jonas Gahr Store");
        authenticatedUser.setEmail("jonas@example.com");

        PrerequisiteCategory category = new PrerequisiteCategory();
        ReflectionTestUtils.setField(category, "id", 30L);
        category.setCategoryName("Renhold av lokaler og utstyr");
        category.setOrganization(organization);

        long nowEpoch = Instant.now().getEpochSecond();
        intervalRule = new IntervalRule();
        ReflectionTestUtils.setField(intervalRule, "id", 7L);
        intervalRule.setIntervalStart(nowEpoch - 600L);
        intervalRule.setIntervalRepeatTime(3600L);

        routine = new PrerequisiteRoutine();
        ReflectionTestUtils.setField(routine, "id", 17L);
        routine.setOrganization(organization);
        routine.setPrerequisiteCategory(category);
        routine.setIntervalRule(intervalRule);
        routine.setTitle("Vask gulvene");
        routine.setDescription("Vask gulvene etter stengetid.");
        routine.setImmediateCorrectiveAction("Kontakt skiftleder hvis gulvvask ikke kan gjennomfores.");
    }

    private OrgUserBridge membership(Long userId) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(organization.getId(), userId));
        bridge.setOrganization(organization);
        return bridge;
    }

    private PrerequisiteRoutineRecord recordWithCreatedAt(LocalDateTime createdAt) {
        PrerequisiteRoutineRecord record = new PrerequisiteRoutineRecord();
        record.setOrganization(organization);
        record.setRoutine(routine);
        record.setPerformedBy(authenticatedUser);
        record.setResultStatus(ResultStatus.COMPLETED);
        ReflectionTestUtils.setField(record, "createdAt", createdAt);
        return record;
    }

    @Nested
    @DisplayName("getAssignedRoutines")
    class GetAssignedRoutinesTests {

        @Test
        @DisplayName("returns current completion state and latest completion time")
        void getAssignedRoutines_success() {
            LocalDateTime currentWindowCompletion = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(intervalRule.getIntervalStart() + 1200L),
                ZoneId.systemDefault()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(prerequisiteRoutineRepository.findAssignedToUserWithDetails(eq(99L), eq(10L), any()))
                .thenReturn(List.of(routine));
            when(prerequisiteRoutineRecordRepository.findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(recordWithCreatedAt(currentWindowCompletion)));

            List<AssignedRoutineResponse> result = routineLoggingService.getAssignedRoutines(99L, 10L);

            assertEquals(1, result.size());
            assertTrue(result.get(0).completedForCurrentInterval());
            assertEquals(currentWindowCompletion, result.get(0).completedAt());
            assertEquals(currentWindowCompletion, result.get(0).lastCompletedAt());
            assertEquals("Renhold av lokaler og utstyr", result.get(0).categoryName());
        }
    }

    @Nested
    @DisplayName("completeRoutine")
    class CompleteRoutineTests {

        @Test
        @DisplayName("creates one completion record for the active interval")
        void completeRoutine_success() {
            PrerequisiteRoutineRecord persistedRecord = new PrerequisiteRoutineRecord();
            persistedRecord.setOrganization(organization);
            persistedRecord.setRoutine(routine);
            persistedRecord.setPerformedBy(authenticatedUser);
            persistedRecord.setResultStatus(ResultStatus.COMPLETED);
            ReflectionTestUtils.setField(persistedRecord, "createdAt", LocalDateTime.now());

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(prerequisiteRoutineRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(routine));
            when(routineUserBridgeRepository.existsByUserAndRoutineAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(true);
            when(prerequisiteRoutineRecordRepository.existsByOrganization_IdAndRoutine_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(10L), eq(17L), any(LocalDateTime.class), any(LocalDateTime.class)
            )).thenReturn(false);
            when(userRepository.findById(99L)).thenReturn(Optional.of(authenticatedUser));
            when(prerequisiteRoutineRecordRepository.save(any(PrerequisiteRoutineRecord.class))).thenReturn(persistedRecord);
            when(prerequisiteRoutineRecordRepository.findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(persistedRecord));

            AssignedRoutineResponse result = routineLoggingService.completeRoutine(17L, 99L, 10L);

            assertTrue(result.completedForCurrentInterval());

            ArgumentCaptor<PrerequisiteRoutineRecord> captor = ArgumentCaptor.forClass(PrerequisiteRoutineRecord.class);
            verify(prerequisiteRoutineRecordRepository).save(captor.capture());
            assertEquals(ResultStatus.COMPLETED, captor.getValue().getResultStatus());
            assertEquals(authenticatedUser, captor.getValue().getPerformedBy());
        }

        @Test
        @DisplayName("rejects duplicate completion in the same interval")
        void completeRoutine_duplicateCompletion() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(prerequisiteRoutineRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(routine));
            when(routineUserBridgeRepository.existsByUserAndRoutineAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(true);
            when(prerequisiteRoutineRecordRepository.existsByOrganization_IdAndRoutine_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(10L), eq(17L), any(LocalDateTime.class), any(LocalDateTime.class)
            )).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routineLoggingService.completeRoutine(17L, 99L, 10L)
            );

            assertEquals("Routine is already completed for the current interval", exception.getMessage());
            verify(prerequisiteRoutineRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects users who are not assigned to the routine")
        void completeRoutine_unassignedUser() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(prerequisiteRoutineRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(routine));
            when(routineUserBridgeRepository.existsByUserAndRoutineAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(false);

            assertThrows(
                AccessDeniedException.class,
                () -> routineLoggingService.completeRoutine(17L, 99L, 10L)
            );
            verify(prerequisiteRoutineRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("marks routines as not completed when the latest record is outside the current interval")
        void getAssignedRoutines_ignoresOldCompletion() {
            LocalDateTime oldCompletion = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(intervalRule.getIntervalStart() - 1200L),
                ZoneId.systemDefault()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(prerequisiteRoutineRepository.findAssignedToUserWithDetails(eq(99L), eq(10L), any()))
                .thenReturn(List.of(routine));
            when(prerequisiteRoutineRecordRepository.findByOrganization_IdAndRoutine_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(recordWithCreatedAt(oldCompletion)));

            List<AssignedRoutineResponse> result = routineLoggingService.getAssignedRoutines(99L, 10L);

            assertFalse(result.get(0).completedForCurrentInterval());
            assertEquals(oldCompletion, result.get(0).lastCompletedAt());
        }
    }
}
