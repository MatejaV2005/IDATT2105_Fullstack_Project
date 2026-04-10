package com.grimni.backend.service;

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

import com.grimni.domain.MappingPoint;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UserDirectoryResponse;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.dto.UserOrgResponse;
import com.grimni.repository.CcpUserBridgeRepository;
import com.grimni.repository.CcpCorrectiveMeasureRepository;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.CertificateRepository;
import com.grimni.repository.CourseLinkRepository;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.CourseResponsibleUserRepository;
import com.grimni.repository.CourseUserProgressRepository;
import com.grimni.repository.DeviationRepository;
import com.grimni.repository.FileCourseBridgeRepository;
import com.grimni.repository.FileObjectRepository;
import com.grimni.repository.InternalControlReviewRepository;
import com.grimni.repository.IntervalRuleRepository;
import com.grimni.repository.MappingPointRepository;
import com.grimni.repository.OrgDangerAnalysisCollaboratorRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.PrerequisiteCategoryRepository;
import com.grimni.repository.PrerequisiteRoutineRecordRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.PrerequisiteStandardRepository;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.repository.RoutineUserBridgeRepository;
import com.grimni.repository.TodoRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.OrganizationService;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgDangerAnalysisCollaboratorRepository dangerAnalysisCollaboratorRepository;

    @Mock
    private CcpUserBridgeRepository ccpUserBridgeRepository;

    @Mock
    private CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository;

    @Mock
    private CcpRecordRepository ccpRecordRepository;

    @Mock
    private CcpRepository ccpRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private RoutineUserBridgeRepository routineUserBridgeRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseResponsibleUserRepository courseResponsibleUserRepository;

    @Mock
    private CourseLinkRepository courseLinkRepository;

    @Mock
    private CourseUserProgressRepository courseUserProgressRepository;

    @Mock
    private FileCourseBridgeRepository fileCourseBridgeRepository;

    @Mock
    private FileObjectRepository fileObjectRepository;

    @Mock
    private InternalControlReviewRepository internalControlReviewRepository;

    @Mock
    private IntervalRuleRepository intervalRuleRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private MappingPointRepository mappingPointRepository;

    @Mock
    private PrerequisiteCategoryRepository prerequisiteCategoryRepository;

    @Mock
    private PrerequisiteStandardRepository prerequisiteStandardRepository;

    @Mock
    private PrerequisiteRoutineRepository prerequisiteRoutineRepository;

    @Mock
    private PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setLegalName("alice");
        testUser.setEmail("alice@test.com");
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    private Organization createOrg(Long id, String name) {
        Organization org = new Organization();
        ReflectionTestUtils.setField(org, "id", id);
        org.setOrgName(name);
        org.setOrgAddress("123 Main St");
        org.setOrgNumber(100);
        org.setAlcoholEnabled(false);
        org.setFoodEnabled(true);
        return org;
    }

    // -------------------------------------------------------------------------
    // createOrganization
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createOrganization")
    class CreateOrganizationTests {

        @Test
        @DisplayName("creates organization and assigns OWNER role to user")
        void createOrganization_success() {
            CreateOrganizationRequest request = new CreateOrganizationRequest("Test Org", "123 Main St", 100, true, false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
                Organization org = inv.getArgument(0);
                ReflectionTestUtils.setField(org, "id", 10L);
                return org;
            });
            when(orgUserBridgeRepository.save(any(OrgUserBridge.class))).thenAnswer(inv -> inv.getArgument(0));

            Organization result = organizationService.createOrganization(request, 1L);

            assertNotNull(result);
            assertEquals("Test Org", result.getOrgName());
            assertEquals("123 Main St", result.getOrgAddress());
            assertEquals(100, result.getOrgNumber());
            assertTrue(result.getAlcoholEnabled());
            assertFalse(result.getFoodEnabled());

            ArgumentCaptor<OrgUserBridge> bridgeCaptor = ArgumentCaptor.forClass(OrgUserBridge.class);
            verify(orgUserBridgeRepository).save(bridgeCaptor.capture());
            OrgUserBridge savedBridge = bridgeCaptor.getValue();
            assertEquals(OrgUserRole.OWNER, savedBridge.getUserRole());
            assertEquals(testUser, savedBridge.getUser());
        }

        
        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void createOrganization_userNotFound_throws() {
            CreateOrganizationRequest request = new CreateOrganizationRequest("Test Org", "123 Main St", 100, false, false);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> organizationService.createOrganization(request, 999L));

            assertEquals("User not found", ex.getMessage());
            verify(organizationRepository, never()).save(any());
            verify(orgUserBridgeRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // findOrganizationsByUserId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findOrganizationsByUserId")
    class FindOrganizationsByUserIdTests {

        @Test
        @DisplayName("returns list of organizations for user")
        void findOrganizationsByUserId_success() {
            Organization org1 = createOrg(1L, "Org A");
            Organization org2 = createOrg(2L, "Org B");

            OrgUserBridge bridge1 = new OrgUserBridge();
            bridge1.setOrganization(org1);
            OrgUserBridge bridge2 = new OrgUserBridge();
            bridge2.setOrganization(org2);

            when(orgUserBridgeRepository.findByUserId(1L)).thenReturn(List.of(bridge1, bridge2));

            List<Organization> result = organizationService.findOrganizationsByUserId(1L);

            assertEquals(2, result.size());
            assertEquals("Org A", result.get(0).getOrgName());
            assertEquals("Org B", result.get(1).getOrgName());
        }

        @Test
        @DisplayName("returns empty list when user has no organizations")
        void findOrganizationsByUserId_noOrgs_returnsEmpty() {
            when(orgUserBridgeRepository.findByUserId(1L)).thenReturn(List.of());

            List<Organization> result = organizationService.findOrganizationsByUserId(1L);

            assertTrue(result.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // findOrganizationById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findOrganizationById")
    class FindOrganizationByIdTests {

        @Test
        @DisplayName("returns organization when found")
        void findOrganizationById_success() {
            Organization org = createOrg(1L, "Test Org");
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

            Organization result = organizationService.findOrganizationById(1L);

            assertNotNull(result);
            assertEquals("Test Org", result.getOrgName());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when organization not found")
        void findOrganizationById_notFound_throws() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> organizationService.findOrganizationById(999L));

            assertEquals("Organization not found", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // updateOrganization
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateOrganization")
    class UpdateOrganizationTests {

        @Test
        @DisplayName("updates all fields when provided")
        void updateOrganization_success() {
            Organization existing = createOrg(1L, "Old Name");
            OrgUserBridge bridge = new OrgUserBridge();
            bridge.setOrganization(existing);
            bridge.setUserRole(OrgUserRole.OWNER);
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("New Name", "456 New St", 200, true, true);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(1L, 1L)).thenReturn(Optional.of(bridge));
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

            Organization result = organizationService.updateOrganization(1L, request, 1L);

            assertEquals("New Name", result.getOrgName());
            assertEquals("456 New St", result.getOrgAddress());
            assertEquals(200, result.getOrgNumber());
            assertTrue(result.getAlcoholEnabled());
            assertTrue(result.getFoodEnabled());
        }

        @Test
        @DisplayName("only updates non-null fields (partial update)")
        void updateOrganization_partialUpdate() {
            Organization existing = createOrg(1L, "Old Name");
            OrgUserBridge bridge = new OrgUserBridge();
            bridge.setOrganization(existing);
            bridge.setUserRole(OrgUserRole.OWNER);
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("New Name", null, null, null, null);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(1L, 1L)).thenReturn(Optional.of(bridge));
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

            Organization result = organizationService.updateOrganization(1L, request, 1L);

            assertEquals("New Name", result.getOrgName());
            assertEquals("123 Main St", result.getOrgAddress());
            assertEquals(100, result.getOrgNumber());
            assertFalse(result.getAlcoholEnabled());
            assertTrue(result.getFoodEnabled());
        }

        @Test
        @DisplayName("throws when user does not belong to organization")
        void updateOrganization_notMember_throws() {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> organizationService.updateOrganization(1L, request, 99L));

            assertEquals("Organization not found", ex.getMessage());
            verify(organizationRepository, never()).findById(any());
            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when organization not found")
        void updateOrganization_notFound_throws() {
            OrgUserBridge bridge = new OrgUserBridge();
            bridge.setUserRole(OrgUserRole.OWNER);
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(999L, 1L)).thenReturn(Optional.of(bridge));
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> organizationService.updateOrganization(999L, request, 1L));

            assertEquals("Organization not found", ex.getMessage());
            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when requester is not owner")
        void updateOrganization_nonOwner_throwsAccessDenied() {
            Organization existing = createOrg(1L, "Old Name");
            OrgUserBridge bridge = new OrgUserBridge();
            bridge.setOrganization(existing);
            bridge.setUserRole(OrgUserRole.MANAGER);
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(1L, 1L)).thenReturn(Optional.of(bridge));

            assertThrows(AccessDeniedException.class,
                    () -> organizationService.updateOrganization(1L, request, 1L));

            verify(organizationRepository, never()).findById(any());
            verify(organizationRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // getAllUsersInOrg
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllUsersInOrg")
    class GetAllUsersInOrgTests {

        @Test
        @DisplayName("returns all users with their roles for the organization")
        void getAllUsersInOrg_success() {
            Organization org = createOrg(10L, "Test Org");

            User user2 = new User();
            ReflectionTestUtils.setField(user2, "id", 2L);
            user2.setLegalName("bob");
            user2.setEmail("bob@test.com");

            OrgUserBridge bridge1 = new OrgUserBridge();
            bridge1.setUser(testUser);
            bridge1.setOrganization(org);
            bridge1.setUserRole(OrgUserRole.OWNER);

            OrgUserBridge bridge2 = new OrgUserBridge();
            bridge2.setUser(user2);
            bridge2.setOrganization(org);
            bridge2.setUserRole(OrgUserRole.WORKER);

            when(orgUserBridgeRepository.findByOrganizationId(10L)).thenReturn(List.of(bridge1, bridge2));

            List<UserOrgResponse> result = organizationService.getAllUsersInOrg(10L);

            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).id());
            assertEquals("alice", result.get(0).legalName());
            assertEquals("alice@test.com", result.get(0).email());
            assertEquals(OrgUserRole.OWNER, result.get(0).accessLevel());
            assertEquals(2L, result.get(1).id());
            assertEquals("bob", result.get(1).legalName());
            assertEquals(OrgUserRole.WORKER, result.get(1).accessLevel());
        }

        @Test
        @DisplayName("returns empty list when organization has no users")
        void getAllUsersInOrg_empty() {
            when(orgUserBridgeRepository.findByOrganizationId(10L)).thenReturn(List.of());

            List<UserOrgResponse> result = organizationService.getAllUsersInOrg(10L);

            assertTrue(result.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // addUserToOrg
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("addUserToOrg")
    class AddUserToOrgTests {

        @Test
        @DisplayName("adds user to organization with given role")
        void addUserToOrg_success() {
            Organization org = createOrg(10L, "Test Org");
            User newUser = new User();
            ReflectionTestUtils.setField(newUser, "id", 5L);
            newUser.setLegalName("charlie");
            newUser.setEmail("charlie@test.com");

            when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
            when(userRepository.findById(5L)).thenReturn(Optional.of(newUser));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 5L)).thenReturn(Optional.empty());
            when(orgUserBridgeRepository.save(any(OrgUserBridge.class))).thenAnswer(inv -> inv.getArgument(0));

            UserOrgResponse result = organizationService.addUserToOrg(5L, OrgUserRole.WORKER, 10L);

            assertEquals(5L, result.id());
            assertEquals("charlie", result.legalName());
            assertEquals("charlie@test.com", result.email());
            assertEquals(OrgUserRole.WORKER, result.accessLevel());
            verify(orgUserBridgeRepository).save(any(OrgUserBridge.class));
        }

        @Test
        @DisplayName("throws when user is already a member")
        void addUserToOrg_alreadyMember_throws() {
            Organization org = createOrg(10L, "Test Org");
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(new OrgUserBridge()));

            assertThrows(IllegalArgumentException.class,
                    () -> organizationService.addUserToOrg(1L, OrgUserRole.WORKER, 10L));

            verify(orgUserBridgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when organization not found")
        void addUserToOrg_orgNotFound_throws() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> organizationService.addUserToOrg(1L, OrgUserRole.WORKER, 999L));

            verify(orgUserBridgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when user not found")
        void addUserToOrg_userNotFound_throws() {
            Organization org = createOrg(10L, "Test Org");
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> organizationService.addUserToOrg(999L, OrgUserRole.WORKER, 10L));

            verify(orgUserBridgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("requester must be owner when using requester-aware add")
        void addUserToOrg_requesterMustBeOwner() {
            OrgUserBridge requesterBridge = new OrgUserBridge();
            requesterBridge.setUserRole(OrgUserRole.MANAGER);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 2L))
                    .thenReturn(Optional.of(requesterBridge));

            assertThrows(AccessDeniedException.class,
                    () -> organizationService.addUserToOrg(5L, OrgUserRole.WORKER, 10L, 2L));
        }
    }

    @Nested
    @DisplayName("removeUserFromOrg")
    class RemoveUserFromOrgTests {

        @Test
        @DisplayName("requester must be owner")
        void removeUserFromOrg_requesterMustBeOwner() {
            OrgUserBridge requesterBridge = new OrgUserBridge();
            requesterBridge.setUserRole(OrgUserRole.MANAGER);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 2L))
                    .thenReturn(Optional.of(requesterBridge));

            assertThrows(AccessDeniedException.class,
                    () -> organizationService.removeUserFromOrg(5L, 10L, 2L));
        }
    }

    @Nested
    @DisplayName("updateUserRoleInOrg")
    class UpdateUserRoleInOrgTests {

        @Test
        @DisplayName("updates a non-owner membership role")
        void updateUserRoleInOrg_success() {
            OrgUserBridge requesterBridge = new OrgUserBridge();
            requesterBridge.setUserRole(OrgUserRole.OWNER);

            OrgUserBridge targetBridge = new OrgUserBridge();
            targetBridge.setUser(testUser);
            targetBridge.setUserRole(OrgUserRole.WORKER);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(requesterBridge));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 5L))
                    .thenReturn(Optional.of(targetBridge));
            when(orgUserBridgeRepository.save(any(OrgUserBridge.class))).thenAnswer(inv -> inv.getArgument(0));

            UserOrgResponse result = organizationService.updateUserRoleInOrg(5L, OrgUserRole.MANAGER, 10L, 1L);

            assertEquals(OrgUserRole.MANAGER, result.accessLevel());
            assertEquals(OrgUserRole.MANAGER, targetBridge.getUserRole());
        }

        @Test
        @DisplayName("cannot demote an owner")
        void updateUserRoleInOrg_demoteOwner_throws() {
            OrgUserBridge requesterBridge = new OrgUserBridge();
            requesterBridge.setUserRole(OrgUserRole.OWNER);

            OrgUserBridge targetBridge = new OrgUserBridge();
            targetBridge.setUser(testUser);
            targetBridge.setUserRole(OrgUserRole.OWNER);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(requesterBridge));
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 5L))
                    .thenReturn(Optional.of(targetBridge));

            assertThrows(IllegalArgumentException.class,
                    () -> organizationService.updateUserRoleInOrg(5L, OrgUserRole.WORKER, 10L, 1L));
        }
    }

    @Nested
    @DisplayName("getUserDirectory")
    class GetUserDirectoryTests {

        @Test
        @DisplayName("returns users that are not already in the organization")
        void getUserDirectory_success() {
            User outsideUser = new User();
            ReflectionTestUtils.setField(outsideUser, "id", 7L);
            outsideUser.setLegalName("bob");
            outsideUser.setEmail("bob@test.com");

            OrgUserBridge existingMember = new OrgUserBridge();
            existingMember.setUser(testUser);

            when(orgUserBridgeRepository.findByOrganizationId(10L)).thenReturn(List.of(existingMember));
            when(userRepository.findAll()).thenReturn(List.of(testUser, outsideUser));

            List<UserDirectoryResponse> result = organizationService.getUserDirectory(10L);

            assertEquals(1, result.size());
            assertEquals(7L, result.getFirst().id());
            assertEquals("bob@test.com", result.getFirst().email());
        }
    }
}
