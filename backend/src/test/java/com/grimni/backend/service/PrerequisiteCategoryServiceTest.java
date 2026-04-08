package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.PrerequisiteCategory;
import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.PrerequisiteStandard;
import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.domain.ids.RoutineUserBridgeId;
import com.grimni.dto.CreatePrerequisiteRoutineRequest;
import com.grimni.dto.PrerequisiteCategoryAllInfoResponse;
import com.grimni.dto.ReplaceRoutineAssignmentsRequest;
import com.grimni.dto.RoutinePrerequisitePointResponse;
import com.grimni.dto.StandardPrerequisitePointResponse;
import com.grimni.dto.UpdatePrerequisiteRoutineRequest;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.IntervalRuleRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteCategoryRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.PrerequisiteStandardRepository;
import com.grimni.repository.RoutineUserBridgeRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.PrerequisiteCategoryService;

@ExtendWith(MockitoExtension.class)
public class PrerequisiteCategoryServiceTest {

    @Mock
    private PrerequisiteCategoryRepository prerequisiteCategoryRepository;

    @Mock
    private PrerequisiteStandardRepository prerequisiteStandardRepository;

    @Mock
    private PrerequisiteRoutineRepository prerequisiteRoutineRepository;

    @Mock
    private RoutineUserBridgeRepository routineUserBridgeRepository;

    @Mock
    private IntervalRuleRepository intervalRuleRepository;

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CcpRepository ccpRepository;

    @InjectMocks
    private PrerequisiteCategoryService prerequisiteCategoryService;

    private Organization organization;
    private PrerequisiteCategory category;
    private User verifier;
    private User performer;
    private PrerequisiteRoutine routine;
    private IntervalRule intervalRule;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", 10L);
        organization.setOrgName("Test Org");

        category = new PrerequisiteCategory();
        ReflectionTestUtils.setField(category, "id", 12L);
        category.setCategoryName("Renhold");
        category.setOrganization(organization);

        verifier = new User();
        ReflectionTestUtils.setField(verifier, "id", 1L);
        verifier.setLegalName("Kari Næss Northun");

        performer = new User();
        ReflectionTestUtils.setField(performer, "id", 2L);
        performer.setLegalName("Jonas Gahr Støre");

        intervalRule = new IntervalRule();
        ReflectionTestUtils.setField(intervalRule, "id", 30L);
        intervalRule.setIntervalStart(1764950400L);
        intervalRule.setIntervalRepeatTime(604800L);

        routine = new PrerequisiteRoutine();
        ReflectionTestUtils.setField(routine, "id", 20L);
        routine.setTitle("Vask gulvene");
        routine.setImmediateCorrectiveAction("Vask ekstra godt neste gang");
        routine.setOrganization(organization);
        routine.setPrerequisiteCategory(category);
        routine.setIntervalRule(intervalRule);
    }

    private OrgUserBridge orgMembership(Long userId) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(organization.getId(), userId));
        bridge.setOrganization(organization);
        return bridge;
    }

    private RoutineUserBridge routineBridge(User user, RoutineUserRole role) {
        RoutineUserBridge bridge = new RoutineUserBridge();
        bridge.setId(new RoutineUserBridgeId(user.getId(), routine.getId(), role));
        bridge.setRoutine(routine);
        bridge.setUser(user);
        bridge.setUserRole(role);
        return bridge;
    }

    @Nested
    @DisplayName("getAllInfo")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns mixed category points and grouped routine users")
        void getAllInfo_success() {
            PrerequisiteStandard standard = new PrerequisiteStandard();
            ReflectionTestUtils.setField(standard, "id", 10L);
            standard.setStandardName("Hold rent");
            standard.setStandardDescription("Vask 1 skal brukes for mat");
            standard.setPrerequisiteCategory(category);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(prerequisiteCategoryRepository.findByOrganization_Id(10L)).thenReturn(List.of(category));
            when(prerequisiteStandardRepository.findByPrerequisiteCategory_Organization_Id(10L)).thenReturn(List.of(standard));
            when(prerequisiteRoutineRepository.findByOrganization_Id(10L)).thenReturn(List.of(routine));
            when(routineUserBridgeRepository.findAllWithUserByRoutineIds(List.of(20L))).thenReturn(List.of(
                routineBridge(verifier, RoutineUserRole.VERIFIER),
                routineBridge(performer, RoutineUserRole.PERFORMER)
            ));

            List<PrerequisiteCategoryAllInfoResponse> result = prerequisiteCategoryService.getAllInfo(99L, 10L);

            assertEquals(1, result.size());
            assertEquals("Renhold", result.get(0).categoryName());
            assertEquals(2, result.get(0).points().size());
            StandardPrerequisitePointResponse standardPoint =
                (StandardPrerequisitePointResponse) result.get(0).points().get(0);
            assertEquals("standard", standardPoint.type());
            RoutinePrerequisitePointResponse routinePoint =
                (RoutinePrerequisitePointResponse) result.get(0).points().get(1);
            assertEquals("routine", routinePoint.type());
            assertEquals("Kari Næss Northun", routinePoint.verifiers().get(0).legalName());
            assertEquals("Jonas Gahr Støre", routinePoint.performers().get(0).legalName());
        }
    }

    @Nested
    @DisplayName("createRoutine")
    class CreateRoutineTests {

        @Test
        @DisplayName("creates interval, routine, and multi-role assignments")
        void createRoutine_success() {
            CreatePrerequisiteRoutineRequest request = new CreatePrerequisiteRoutineRequest(
                "Vask gulvene",
                "Vask ekstra godt neste gang",
                1764950400L,
                604800L,
                List.of(1L),
                List.of(),
                List.of(2L),
                List.of()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(prerequisiteCategoryRepository.findByIdAndOrganization_Id(12L, 10L)).thenReturn(Optional.of(category));
            when(intervalRuleRepository.save(any(IntervalRule.class))).thenAnswer(invocation -> {
                IntervalRule savedInterval = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedInterval, "id", 30L);
                return savedInterval;
            });
            when(prerequisiteRoutineRepository.save(any(PrerequisiteRoutine.class))).thenAnswer(invocation -> {
                PrerequisiteRoutine savedRoutine = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedRoutine, "id", 20L);
                return savedRoutine;
            });
            when(userRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(verifier, performer));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserIdIn(10L, Set.of(1L, 2L))).thenReturn(List.of(
                orgMembership(1L),
                orgMembership(2L)
            ));
            when(routineUserBridgeRepository.saveAll(anyCollection())).thenAnswer(invocation -> List.copyOf(invocation.getArgument(0)));

            RoutinePrerequisitePointResponse result = prerequisiteCategoryService.createRoutine(12L, request, 99L, 10L);

            assertEquals(20L, result.id());
            assertEquals(30L, result.interval().intervalId());
            assertEquals(1, result.verifiers().size());
            assertEquals(1, result.performers().size());

            ArgumentCaptor<Collection<RoutineUserBridge>> bridgesCaptor = ArgumentCaptor.forClass(Collection.class);
            verify(routineUserBridgeRepository).saveAll(bridgesCaptor.capture());
            assertEquals(2, bridgesCaptor.getValue().size());
        }
    }

    @Nested
    @DisplayName("updateRoutine")
    class UpdateRoutineTests {

        @Test
        @DisplayName("updates routine content without replacing assignments")
        void updateRoutine_doesNotTouchAssignments() {
            UpdatePrerequisiteRoutineRequest request = new UpdatePrerequisiteRoutineRequest(
                "Ny tittel",
                null,
                null,
                null,
                null
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(prerequisiteRoutineRepository.findByIdAndOrganization_Id(20L, 10L)).thenReturn(Optional.of(routine));
            when(prerequisiteRoutineRepository.save(any(PrerequisiteRoutine.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(routineUserBridgeRepository.findAllWithUserByRoutineIds(List.of(20L))).thenReturn(List.of(
                routineBridge(verifier, RoutineUserRole.VERIFIER)
            ));

            RoutinePrerequisitePointResponse result = prerequisiteCategoryService.updateRoutine(20L, request, 99L, 10L);

            assertEquals("Ny tittel", result.title());
            assertEquals(1, result.verifiers().size());
            verify(routineUserBridgeRepository, never()).deleteByRoutine_Id(any());
            verify(routineUserBridgeRepository, never()).saveAll(anyCollection());
        }
    }

    @Nested
    @DisplayName("replaceRoutineAssignments")
    class ReplaceRoutineAssignmentsTests {

        @Test
        @DisplayName("rejects users outside the active organization")
        void replaceRoutineAssignments_invalidUsers() {
            ReplaceRoutineAssignmentsRequest request = new ReplaceRoutineAssignmentsRequest(
                List.of(1L),
                List.of(),
                List.of(),
                List.of()
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(prerequisiteRoutineRepository.findByIdAndOrganization_Id(20L, 10L)).thenReturn(Optional.of(routine));
            when(userRepository.findAllById(Set.of(1L))).thenReturn(List.of(verifier));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserIdIn(10L, Set.of(1L))).thenReturn(List.of());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prerequisiteCategoryService.replaceRoutineAssignments(20L, request, 99L, 10L)
            );

            assertEquals("Assigned users must belong to the active organization", exception.getMessage());
            verify(routineUserBridgeRepository, never()).deleteByRoutine_Id(any());
            verify(routineUserBridgeRepository, never()).saveAll(anyCollection());
        }
    }
}
