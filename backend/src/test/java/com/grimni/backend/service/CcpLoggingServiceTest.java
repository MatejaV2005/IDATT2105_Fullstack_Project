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

import java.math.BigDecimal;
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

import com.grimni.domain.Ccp;
import com.grimni.domain.CcpRecord;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.AssignedCcpResponse;
import com.grimni.dto.CreateCcpRecordRequest;
import com.grimni.dto.SubmittedCcpRecordResponse;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.CcpUserBridgeRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.CcpLoggingService;

@ExtendWith(MockitoExtension.class)
public class CcpLoggingServiceTest {

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private CcpRepository ccpRepository;

    @Mock
    private CcpUserBridgeRepository ccpUserBridgeRepository;

    @Mock
    private CcpRecordRepository ccpRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CcpLoggingService ccpLoggingService;

    private Organization organization;
    private User authenticatedUser;
    private Ccp ccp;
    private IntervalRule intervalRule;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", 10L);
        organization.setOrgName("Test Org");

        authenticatedUser = new User();
        ReflectionTestUtils.setField(authenticatedUser, "id", 99L);
        authenticatedUser.setLegalName("Routine Worker");
        authenticatedUser.setEmail("worker@example.com");

        long nowEpoch = Instant.now().getEpochSecond();
        intervalRule = new IntervalRule();
        ReflectionTestUtils.setField(intervalRule, "id", 7L);
        intervalRule.setIntervalStart(nowEpoch - 600L);
        intervalRule.setIntervalRepeatTime(3600L);

        ccp = new Ccp();
        ReflectionTestUtils.setField(ccp, "id", 17L);
        ccp.setOrganization(organization);
        ccp.setIntervalRule(intervalRule);
        ccp.setCcpName("Kjolerom");
        ccp.setMonitoredDescription("Temperatur i kjolerom");
        ccp.setCriticalMin(BigDecimal.ZERO);
        ccp.setCriticalMax(BigDecimal.valueOf(4));
        ccp.setUnit("C");
        ccp.setImmediateCorrectiveAction("Flytt varer til reservekjol.");
    }

    private OrgUserBridge membership(Long userId) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(organization.getId(), userId));
        bridge.setOrganization(organization);
        return bridge;
    }

    private CcpRecord recordWithCreatedAt(LocalDateTime createdAt, BigDecimal measuredValue) {
        CcpRecord record = new CcpRecord();
        record.setOrganization(organization);
        record.setCcp(ccp);
        record.setPerformedBy(authenticatedUser);
        record.setMeasuredValue(measuredValue);
        ReflectionTestUtils.setField(record, "createdAt", createdAt);
        return record;
    }

    @Nested
    @DisplayName("getAssignedCcps")
    class GetAssignedCcpsTests {

        @Test
        @DisplayName("returns current completion state and latest measurement")
        void getAssignedCcps_success() {
            LocalDateTime currentWindowCompletion = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(intervalRule.getIntervalStart() + 1200L),
                ZoneId.systemDefault()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(ccpRepository.findAssignedToUserWithDetails(eq(99L), eq(10L), any()))
                .thenReturn(List.of(ccp));
            when(ccpRecordRepository.findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(recordWithCreatedAt(currentWindowCompletion, BigDecimal.valueOf(3.5))));

            List<AssignedCcpResponse> result = ccpLoggingService.getAssignedCcps(99L, 10L);

            assertEquals(1, result.size());
            assertTrue(result.get(0).completedForCurrentInterval());
            assertEquals(currentWindowCompletion, result.get(0).completedAt());
            assertEquals(currentWindowCompletion, result.get(0).lastCompletedAt());
            assertEquals(BigDecimal.valueOf(3.5), result.get(0).lastMeasuredValue());
        }

        @Test
        @DisplayName("ignores old completion outside the active interval")
        void getAssignedCcps_ignoresOldCompletion() {
            LocalDateTime oldCompletion = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(intervalRule.getIntervalStart() - 1200L),
                ZoneId.systemDefault()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(ccpRepository.findAssignedToUserWithDetails(eq(99L), eq(10L), any()))
                .thenReturn(List.of(ccp));
            when(ccpRecordRepository.findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(recordWithCreatedAt(oldCompletion, BigDecimal.valueOf(2.5))));

            List<AssignedCcpResponse> result = ccpLoggingService.getAssignedCcps(99L, 10L);

            assertFalse(result.get(0).completedForCurrentInterval());
            assertEquals(oldCompletion, result.get(0).lastCompletedAt());
            assertEquals(BigDecimal.valueOf(2.5), result.get(0).lastMeasuredValue());
        }
    }

    @Nested
    @DisplayName("createRecord")
    class CreateRecordTests {

        @Test
        @DisplayName("creates one record for the active interval and flags out-of-range values")
        void createRecord_success() {
            CcpRecord persistedRecord = new CcpRecord();
            ReflectionTestUtils.setField(persistedRecord, "id", 88L);
            persistedRecord.setOrganization(organization);
            persistedRecord.setCcp(ccp);
            persistedRecord.setPerformedBy(authenticatedUser);
            persistedRecord.setMeasuredValue(BigDecimal.valueOf(5.5));
            ReflectionTestUtils.setField(persistedRecord, "createdAt", LocalDateTime.now());

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(ccp));
            when(ccpUserBridgeRepository.existsByUserAndCcpAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(true);
            when(ccpRecordRepository.existsByOrganization_IdAndCcp_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(10L), eq(17L), any(LocalDateTime.class), any(LocalDateTime.class)
            )).thenReturn(false);
            when(userRepository.findById(99L)).thenReturn(Optional.of(authenticatedUser));
            when(ccpRecordRepository.save(any(CcpRecord.class))).thenReturn(persistedRecord);
            when(ccpRecordRepository.findByOrganization_IdAndCcp_IdInOrderByCreatedAtDesc(10L, List.of(17L)))
                .thenReturn(List.of(persistedRecord));

            SubmittedCcpRecordResponse result = ccpLoggingService.createRecord(
                17L,
                new CreateCcpRecordRequest(BigDecimal.valueOf(5.5), "For hoy verdi"),
                99L,
                10L
            );

            assertTrue(result.ccp().completedForCurrentInterval());
            assertEquals(88L, result.recordId());
            assertTrue(result.outsideCriticalRange());

            ArgumentCaptor<CcpRecord> captor = ArgumentCaptor.forClass(CcpRecord.class);
            verify(ccpRecordRepository).save(captor.capture());
            assertEquals(BigDecimal.valueOf(5.5), captor.getValue().getMeasuredValue());
            assertEquals("For hoy verdi", captor.getValue().getComment());
            assertEquals(ccp.getCriticalMin(), captor.getValue().getCriticalMin());
            assertEquals(ccp.getCriticalMax(), captor.getValue().getCriticalMax());
        }

        @Test
        @DisplayName("rejects duplicate completion in the same interval")
        void createRecord_duplicateCompletion() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(ccp));
            when(ccpUserBridgeRepository.existsByUserAndCcpAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(true);
            when(ccpRecordRepository.existsByOrganization_IdAndCcp_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(10L), eq(17L), any(LocalDateTime.class), any(LocalDateTime.class)
            )).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ccpLoggingService.createRecord(17L, new CreateCcpRecordRequest(BigDecimal.valueOf(3), null), 99L, 10L)
            );

            assertEquals("Critical control point is already completed for the current interval", exception.getMessage());
            verify(ccpRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects users who are not assigned to the CCP")
        void createRecord_unassignedUser() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(membership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(ccp));
            when(ccpUserBridgeRepository.existsByUserAndCcpAndRoles(eq(99L), eq(17L), eq(10L), any())).thenReturn(false);

            assertThrows(
                AccessDeniedException.class,
                () -> ccpLoggingService.createRecord(17L, new CreateCcpRecordRequest(BigDecimal.valueOf(3), null), 99L, 10L)
            );
            verify(ccpRecordRepository, never()).save(any());
        }
    }
}
