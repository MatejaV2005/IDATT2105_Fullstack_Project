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
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.OrganizationService;

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

    @InjectMocks
    private OrganizationService organizationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("alice");
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
        @DisplayName("throws RuntimeException when user not found")
        void createOrganization_userNotFound_throws() {
            CreateOrganizationRequest request = new CreateOrganizationRequest("Test Org", "123 Main St", 100, false, false);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
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
        @DisplayName("throws RuntimeException when organization not found")
        void findOrganizationById_notFound_throws() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
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

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> organizationService.updateOrganization(1L, request, 99L));

            assertEquals("Organization not found", ex.getMessage());
            verify(organizationRepository, never()).findById(any());
            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when organization not found")
        void updateOrganization_notFound_throws() {
            OrgUserBridge bridge = new OrgUserBridge();
            UpdateOrganizationRequest request = new UpdateOrganizationRequest("Name", "Addr", 100, false, false);

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(999L, 1L)).thenReturn(Optional.of(bridge));
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> organizationService.updateOrganization(999L, request, 1L));

            assertEquals("Organization not found", ex.getMessage());
            verify(organizationRepository, never()).save(any());
        }
    }
}