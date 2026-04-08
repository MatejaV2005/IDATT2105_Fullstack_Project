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
import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.ProductCategory;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.CcpUserBridgeId;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.CcpResponse;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.repository.CcpCorrectiveMeasureRepository;
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
}
