package com.grimni.backend.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.CcpRecord;
import com.grimni.domain.Deviation;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;
import com.grimni.dto.CreateDeviationRequest;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.DeviationRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.DeviationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviationServiceTest {

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CcpRecordRepository ccpRecordRepository;

    @InjectMocks
    private DeviationService deviationService;

    private User testUser;
    private Organization testOrg;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setLegalName("Test User");
        testUser.setEmail("test@example.com");
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testOrg = new Organization();
        testOrg.setOrgName("Test Org");
        ReflectionTestUtils.setField(testOrg, "id", 1L);
    }

    private Deviation createDeviation(Long id) {
        Deviation deviation = new Deviation();
        ReflectionTestUtils.setField(deviation, "id", id);
        deviation.setWhatWentWrong("Something went wrong");
        deviation.setImmediateActionTaken("Immediate action");
        deviation.setPotentialCause("Potential cause");
        deviation.setPotentialPreventativeMeasure("Preventative measure");
        deviation.setPreventativeMeasureActuallyTaken("");
        deviation.setCategory(DeviationCategory.OTHER);
        deviation.setReviewStatus(ReviewStatus.OPEN);
        return deviation;
    }

    // -------------------------------------------------------------------------
    // createDeviation
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createDeviation")
    class CreateDeviationTests {

        @Test
        @DisplayName("creates deviation successfully")
        void createDeviation_success() {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, null, null, DeviationCategory.IK_MAT,
                    "What went wrong",
                    "Immediate action taken",
                    "Potential cause",
                    "Potential preventative measure"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
            when(deviationRepository.save(any(Deviation.class))).thenAnswer(inv -> {
                Deviation d = inv.getArgument(0);
                ReflectionTestUtils.setField(d, "id", 10L);
                return d;
            });

            Deviation result = deviationService.createDeviation(request, 1L);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("What went wrong", result.getWhatWentWrong());
            assertEquals("Immediate action taken", result.getImmediateActionTaken());
            assertEquals("Potential cause", result.getPotentialCause());
            assertEquals("Potential preventative measure", result.getPotentialPreventativeMeasure());
            assertEquals(DeviationCategory.IK_MAT, result.getCategory());
            assertEquals(ReviewStatus.OPEN, result.getReviewStatus());
            assertEquals(testUser, result.getReportedBy());
            assertEquals(testOrg, result.getOrganization());
            assertEquals("", result.getPreventativeMeasureActuallyTaken());

            verify(deviationRepository).save(any(Deviation.class));
        }

        @Test
        @DisplayName("creates deviation with CCP record when provided")
        void createDeviation_withCcpRecord_success() {
            CcpRecord ccpRecord = new CcpRecord();
            ReflectionTestUtils.setField(ccpRecord, "id", 5L);

            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, 5L, null, DeviationCategory.IK_MAT,
                    "Temperature too high",
                    "Checked temperature",
                    "Refrigerator malfunction",
                    "Regular maintenance"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
            when(ccpRecordRepository.findById(5L)).thenReturn(Optional.of(ccpRecord));
            when(deviationRepository.save(any(Deviation.class))).thenAnswer(inv -> inv.getArgument(0));

            Deviation result = deviationService.createDeviation(request, 1L);

            assertNotNull(result);
            assertEquals(ccpRecord, result.getCcpRecord());
            verify(ccpRecordRepository).findById(5L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void createDeviation_userNotFound_throws() {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, null, null, DeviationCategory.OTHER,
                    "What went wrong",
                    "Immediate action",
                    "Potential cause",
                    "Preventative measure"
            );

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.createDeviation(request, 999L));

            assertEquals("User not found", ex.getMessage());
            verify(deviationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when organization not found")
        void createDeviation_organizationNotFound_throws() {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    999L, null, null, DeviationCategory.OTHER,
                    "What went wrong",
                    "Immediate action",
                    "Potential cause",
                    "Preventative measure"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.createDeviation(request, 1L));

            assertEquals("Organization not found", ex.getMessage());
            verify(deviationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when CCP record not found")
        void createDeviation_ccpNotFound_throws() {
            CreateDeviationRequest request = new CreateDeviationRequest(
                    1L, 999L, null, DeviationCategory.OTHER,
                    "What went wrong",
                    "Immediate action",
                    "Potential cause",
                    "Preventative measure"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
            when(ccpRecordRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.createDeviation(request, 1L));

            assertEquals("CCP Record not found", ex.getMessage());
            verify(deviationRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // getAllDeviations
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllDeviations")
    class GetAllDeviationsTests {

        @Test
        @DisplayName("returns all deviations")
        void getAllDeviations_success() {
            Deviation d1 = createDeviation(1L);
            Deviation d2 = createDeviation(2L);
            d2.setWhatWentWrong("Another issue");

            when(deviationRepository.findAll()).thenReturn(List.of(d1, d2));

            List<Deviation> result = deviationService.getAllDeviations();

            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals(2L, result.get(1).getId());
            verify(deviationRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no deviations")
        void getAllDeviations_empty_returnsEmptyList() {
            when(deviationRepository.findAll()).thenReturn(List.of());

            List<Deviation> result = deviationService.getAllDeviations();

            assertTrue(result.isEmpty());
            verify(deviationRepository).findAll();
        }
    }

    // -------------------------------------------------------------------------
    // getDeviationById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getDeviationById")
    class GetDeviationByIdTests {

        @Test
        @DisplayName("returns deviation when found")
        void getDeviationById_success() {
            Deviation deviation = createDeviation(1L);
            when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

            Deviation result = deviationService.getDeviationById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Something went wrong", result.getWhatWentWrong());
            verify(deviationRepository).findById(1L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when deviation not found")
        void getDeviationById_notFound_throws() {
            when(deviationRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.getDeviationById(999L));

            assertEquals("Deviation not found", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // resolveDeviation
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("resolveDeviation")
    class ResolveDeviationTests {

        @Test
        @DisplayName("resolves deviation successfully")
        void resolveDeviation_success() {
            Deviation deviation = createDeviation(1L);
            User reviewer = new User();
            ReflectionTestUtils.setField(reviewer, "id", 2L);
            reviewer.setLegalName("Reviewer");

            when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
            when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
            when(deviationRepository.save(any(Deviation.class))).thenAnswer(inv -> inv.getArgument(0));

            Deviation result = deviationService.resolveDeviation(1L, 2L, "Fixed the issue");

            assertEquals(ReviewStatus.CLOSED, result.getReviewStatus());
            assertEquals(reviewer, result.getReviewedBy());
            assertEquals("Fixed the issue", result.getPreventativeMeasureActuallyTaken());
            assertNotNull(result.getReviewedAt());
            verify(deviationRepository).save(deviation);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when deviation not found")
        void resolveDeviation_deviationNotFound_throws() {
            when(deviationRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.resolveDeviation(999L, 1L, "Fix"));

            assertEquals("Deviation not found", ex.getMessage());
            verify(deviationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when reviewer not found")
        void resolveDeviation_reviewerNotFound_throws() {
            Deviation deviation = createDeviation(1L);
            when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> deviationService.resolveDeviation(1L, 999L, "Fix"));

            assertEquals("User not found", ex.getMessage());
            verify(deviationRepository, never()).save(any());
        }
    }
}