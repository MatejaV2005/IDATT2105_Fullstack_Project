package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.Ccp;
import com.grimni.domain.CcpCorrectiveMeasure;
import com.grimni.domain.CcpRecord;
import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.CcpUserBridgeId;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.CcpHistoryResponse;
import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.CcpResponse;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.repository.CcpCorrectiveMeasureRepository;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.CcpUserBridgeRepository;
import com.grimni.repository.IntervalRuleRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.CcpService;

@ExtendWith(MockitoExtension.class)
public class CcpServiceTest {

    @Mock
    private CcpRepository ccpRepository;

    @Mock
    private CcpUserBridgeRepository ccpUserBridgeRepository;

    @Mock
    private CcpRecordRepository ccpRecordRepository;

    @Mock
    private CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private IntervalRuleRepository intervalRuleRepository;

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrerequisiteRoutineRepository prerequisiteRoutineRepository;

    @InjectMocks
    private CcpService ccpService;

    private Organization organization;
    private Ccp ccp;
    private ProductCategory productCategory;
    private User verifier;
    private IntervalRule intervalRule;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", 10L);
        organization.setOrgName("Test Org");

        productCategory = new ProductCategory();
        ReflectionTestUtils.setField(productCategory, "id", 90L);
        productCategory.setProductDescription("Burger");
        productCategory.setOrganization(organization);

        verifier = new User();
        ReflectionTestUtils.setField(verifier, "id", 1L);
        verifier.setLegalName("Kari Næss Northun");

        intervalRule = new IntervalRule();
        ReflectionTestUtils.setField(intervalRule, "id", 30L);
        intervalRule.setIntervalStart(1764950400L);
        intervalRule.setIntervalRepeatTime(1800L);

        ccp = new Ccp();
        ReflectionTestUtils.setField(ccp, "id", 33L);
        ccp.setCcpName("Varmholding ved servering");
        ccp.setHow("Måles hver 30. minutt.");
        ccp.setEquipment("Digitalt stikktermometer.");
        ccp.setInstructionsAndCalibration("Rengjør termometer.");
        ccp.setImmediateCorrectiveAction("Øk varme umiddelbart.");
        ccp.setCriticalMin(new BigDecimal("60.0"));
        ccp.setCriticalMax(new BigDecimal("90.0"));
        ccp.setUnit("C");
        ccp.setMonitoredDescription("Supper og sauser.");
        ccp.setOrganization(organization);
        ccp.setIntervalRule(intervalRule);
    }

    private OrgUserBridge orgMembership(Long userId) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(organization.getId(), userId));
        bridge.setOrganization(organization);
        return bridge;
    }

    private CcpUserBridge ccpBridge(User user, RoutineUserRole role) {
        CcpUserBridge bridge = new CcpUserBridge();
        bridge.setId(new CcpUserBridgeId(user.getId(), ccp.getId(), role));
        bridge.setCcp(ccp);
        bridge.setUser(user);
        bridge.setUserRole(role);
        return bridge;
    }

    private CcpCorrectiveMeasure correctiveMeasure(Long id, String productName, String description) {
        ProductCategory category = new ProductCategory();
        ReflectionTestUtils.setField(category, "id", id + 100);
        category.setProductDescription(productName);
        category.setOrganization(organization);

        CcpCorrectiveMeasure measure = new CcpCorrectiveMeasure();
        ReflectionTestUtils.setField(measure, "id", id);
        measure.setCcp(ccp);
        measure.setProductCategory(category);
        measure.setMeasureDescription(description);
        return measure;
    }

    @Nested
    @DisplayName("getAllInfo")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns grouped users and product names")
        void getAllInfo_success() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(ccpRepository.findByOrganization_Id(10L)).thenReturn(List.of(ccp));
            when(ccpUserBridgeRepository.findAllWithUserByCcpIds(List.of(33L))).thenReturn(List.of(
                ccpBridge(verifier, RoutineUserRole.VERIFIER)
            ));
            when(ccpCorrectiveMeasureRepository.findAllWithProductCategoryByCcpIds(List.of(33L))).thenReturn(List.of(
                correctiveMeasure(501L, "Burger", "Fortsett oppvarming")
            ));

            List<CcpResponse> result = ccpService.getAllInfo(99L, 10L);

            assertEquals(1, result.size());
            assertEquals("Varmholding ved servering", result.get(0).name());
            assertEquals("Kari Næss Northun", result.get(0).verifiers().get(0).legalName());
            assertEquals("Burger", result.get(0).ccpCorrectiveMeasures().get(0).productName());
        }
    }

    @Nested
    @DisplayName("createCcp")
    class CreateCcpTests {

        @Test
        @DisplayName("creates interval, CCP, assignments, and corrective measures")
        void createCcp_success() {
            User performer = new User();
            ReflectionTestUtils.setField(performer, "id", 2L);
            performer.setLegalName("Jens Stoltenberg");

            CreateCcpRequest request = new CreateCcpRequest(
                "Varmholding ved servering",
                "Måles hver 30. minutt.",
                "Digitalt stikktermometer.",
                "Rengjør termometer.",
                "Øk varme umiddelbart.",
                new BigDecimal("60.0"),
                new BigDecimal("90.0"),
                "C",
                "Supper og sauser.",
                1764950400L,
                1800L,
                List.of(1L),
                List.of(),
                List.of(2L),
                List.of(),
                List.of(new CreateCcpCorrectiveMeasureRequest(90L, "Fortsett oppvarming"))
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(intervalRuleRepository.save(any(IntervalRule.class))).thenAnswer(invocation -> {
                IntervalRule savedInterval = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedInterval, "id", 30L);
                return savedInterval;
            });
            when(ccpRepository.save(any(Ccp.class))).thenAnswer(invocation -> {
                Ccp savedCcp = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedCcp, "id", 33L);
                return savedCcp;
            });
            when(userRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(verifier, performer));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserIdIn(10L, Set.of(1L, 2L))).thenReturn(List.of(
                orgMembership(1L),
                orgMembership(2L)
            ));
            when(ccpUserBridgeRepository.saveAll(anyCollection())).thenAnswer(invocation -> List.copyOf(invocation.getArgument(0)));
            when(productCategoryRepository.findByOrganization_IdAndIdIn(10L, Set.of(90L))).thenReturn(List.of(productCategory));
            when(ccpCorrectiveMeasureRepository.saveAll(anyCollection())).thenAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                List<CcpCorrectiveMeasure> measures = List.copyOf((Collection<CcpCorrectiveMeasure>) invocation.getArgument(0));
                ReflectionTestUtils.setField(measures.get(0), "id", 501L);
                return measures;
            });

            CcpResponse result = ccpService.createCcp(request, 99L, 10L);

            assertEquals(33L, result.id());
            assertEquals(1, result.verifiers().size());
            assertEquals(1, result.performers().size());
            assertEquals(1, result.ccpCorrectiveMeasures().size());

            ArgumentCaptor<Collection<CcpUserBridge>> assignmentsCaptor = ArgumentCaptor.forClass(Collection.class);
            verify(ccpUserBridgeRepository).saveAll(assignmentsCaptor.capture());
            assertEquals(2, assignmentsCaptor.getValue().size());
        }
    }

    @Nested
    @DisplayName("replaceAssignments")
    class ReplaceAssignmentsTests {

        @Test
        @DisplayName("rejects users outside the active organization")
        void replaceAssignments_invalidUsers() {
            ReplaceCcpAssignmentsRequest request = new ReplaceCcpAssignmentsRequest(
                List.of(1L),
                List.of(),
                List.of(),
                List.of()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(33L, 10L)).thenReturn(Optional.of(ccp));
            when(userRepository.findAllById(Set.of(1L))).thenReturn(List.of(verifier));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserIdIn(10L, Set.of(1L))).thenReturn(List.of());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ccpService.replaceAssignments(33L, request, 99L, 10L)
            );

            assertEquals("Assigned users must belong to the active organization", exception.getMessage());
            verify(ccpUserBridgeRepository, never()).deleteByCcp_Id(any());
            verify(ccpUserBridgeRepository, never()).saveAll(anyCollection());
        }
    }

    @Nested
    @DisplayName("correctiveMeasures")
    class CorrectiveMeasureTests {

        @Test
        @DisplayName("rejects product categories outside the active organization")
        void createCorrectiveMeasure_invalidProductCategory() {
            CreateCcpCorrectiveMeasureRequest request = new CreateCcpCorrectiveMeasureRequest(90L, "Fortsett oppvarming");

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(33L, 10L)).thenReturn(Optional.of(ccp));
            when(productCategoryRepository.findByIdAndOrganization_Id(90L, 10L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ccpService.createCorrectiveMeasure(33L, request, 99L, 10L)
            );

            assertEquals("Product category must belong to the active organization", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteCcp")
    class DeleteCcpTests {

        @Test
        @DisplayName("deletes dependent links and cleans up unused interval")
        void deleteCcp_cleansUpInterval() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(ccpRepository.findByIdAndOrganization_Id(33L, 10L)).thenReturn(Optional.of(ccp));
            when(ccpRepository.existsByIntervalRule_Id(30L)).thenReturn(false);
            when(prerequisiteRoutineRepository.existsByIntervalRule_Id(30L)).thenReturn(false);

            ccpService.deleteCcp(33L, 99L, 10L);

            verify(ccpUserBridgeRepository).deleteByCcp_Id(33L);
            verify(ccpCorrectiveMeasureRepository).deleteByCcp_Id(33L);
            verify(ccpRepository).delete(ccp);
            verify(intervalRuleRepository).deleteById(30L);
        }
    }

    @Nested
    @DisplayName("getVerificationCount")
    class GetVerificationCountTests {

        @Test
        @DisplayName("returns count for a VERIFIER user")
        void getVerificationCount_verifier() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgMembership(1L)));
            when(ccpRecordRepository.countWaitingVerifications(10L, 1L, false)).thenReturn(3L);

            long count = ccpService.getVerificationCount(1L, 10L, "WORKER");

            assertEquals(3L, count);
        }

        @Test
        @DisplayName("passes isManagerOrOwner=true for MANAGER role")
        void getVerificationCount_manager() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgMembership(1L)));
            when(ccpRecordRepository.countWaitingVerifications(10L, 1L, true)).thenReturn(5L);

            long count = ccpService.getVerificationCount(1L, 10L, "MANAGER");

            assertEquals(5L, count);
        }

        @Test
        @DisplayName("throws when user is not in org")
        void getVerificationCount_notInOrg() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> ccpService.getVerificationCount(1L, 10L, "WORKER"));
        }
    }

    @Nested
    @DisplayName("getVerificationLogs")
    class GetVerificationLogsTests {

        @Test
        @DisplayName("returns records grouped by CCP")
        void getVerificationLogs_grouped() {
            User performer = new User();
            ReflectionTestUtils.setField(performer, "id", 5L);
            performer.setLegalName("Per Willy Amundsen");

            CcpRecord record1 = new CcpRecord();
            ReflectionTestUtils.setField(record1, "id", 11L);
            record1.setCcp(ccp);
            record1.setCcpName(ccp.getCcpName());
            record1.setMeasuredValue(new BigDecimal("2.4"));
            record1.setCriticalMin(new BigDecimal("2"));
            record1.setCriticalMax(new BigDecimal("4"));
            record1.setUnit("C");
            record1.setComment("Kjøleskapet lager en rar lyd");
            record1.setPerformedBy(performer);

            CcpRecord record2 = new CcpRecord();
            ReflectionTestUtils.setField(record2, "id", 12L);
            record2.setCcp(ccp);
            record2.setCcpName(ccp.getCcpName());
            record2.setMeasuredValue(new BigDecimal("8"));
            record2.setCriticalMin(new BigDecimal("2"));
            record2.setCriticalMax(new BigDecimal("4"));
            record2.setUnit("C");
            record2.setComment("Midlertidig høy temperatur");
            record2.setPerformedBy(verifier);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgMembership(1L)));
            when(ccpRecordRepository.findWaitingVerificationRecords(10L, 1L, false)).thenReturn(List.of(record1, record2));

            List<CcpHistoryResponse> result = ccpService.getVerificationLogs(1L, 10L, "WORKER");

            assertEquals(1, result.size());
            assertEquals(33L, result.get(0).id());
            assertEquals("Varmholding ved servering", result.get(0).name());
            assertEquals(2, result.get(0).records().size());
            assertEquals(new BigDecimal("2.4"), result.get(0).records().get(0).value());
            assertEquals("Per Willy Amundsen", result.get(0).records().get(0).performedBy().legalName());
        }

        @Test
        @DisplayName("returns empty list when no records")
        void getVerificationLogs_empty() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgMembership(1L)));
            when(ccpRecordRepository.findWaitingVerificationRecords(10L, 1L, true)).thenReturn(List.of());

            List<CcpHistoryResponse> result = ccpService.getVerificationLogs(1L, 10L, "OWNER");

            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("handles orphan records with null CCP")
        void getVerificationLogs_orphanRecords() {
            CcpRecord orphan = new CcpRecord();
            ReflectionTestUtils.setField(orphan, "id", 20L);
            orphan.setCcp(null);
            orphan.setCcpName("Deleted CCP");
            orphan.setMeasuredValue(new BigDecimal("5.0"));
            orphan.setCriticalMin(new BigDecimal("2"));
            orphan.setCriticalMax(new BigDecimal("8"));
            orphan.setUnit("C");
            orphan.setComment(null);
            orphan.setPerformedBy(null);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgMembership(1L)));
            when(ccpRecordRepository.findWaitingVerificationRecords(10L, 1L, true)).thenReturn(List.of(orphan));

            List<CcpHistoryResponse> result = ccpService.getVerificationLogs(1L, 10L, "MANAGER");

            assertEquals(1, result.size());
            assertEquals(null, result.get(0).id());
            assertEquals("Deleted CCP", result.get(0).name());
            assertEquals(null, result.get(0).records().get(0).performedBy());
        }
    }
}
