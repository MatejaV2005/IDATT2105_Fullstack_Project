package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.grimni.domain.MappingPoint;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.CreateMappingPointRequest;
import com.grimni.dto.MappingPointResponse;
import com.grimni.dto.UpdateMappingPointRequest;
import com.grimni.repository.MappingPointRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.service.MappingPointService;

@ExtendWith(MockitoExtension.class)
public class MappingPointServiceTest {

    @Mock
    private MappingPointRepository mappingPointRepository;

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @InjectMocks
    private MappingPointService mappingPointService;

    private Organization organization;
    private MappingPoint mappingPoint;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", 10L);
        organization.setOrgName("Test Org");

        mappingPoint = new MappingPoint();
        ReflectionTestUtils.setField(mappingPoint, "id", 17L);
        mappingPoint.setLaw("AL § 1-5");
        mappingPoint.setSeverityDots((short) 8);
        mappingPoint.setTitle("Salg eller utlevering til person som er under 18 år");
        mappingPoint.setChallenges("Mindreårige kunder bruker lånt/falskt ID-kort.");
        mappingPoint.setMeasures("Instruks om å sjekke legitimasjon.");
        mappingPoint.setResponsibleForPoint("Hvem enn som er i kassen ved gitt tidspunkt");
        mappingPoint.setOrganization(organization);
    }

    private OrgUserBridge orgMembership(Long userId) {
        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(organization.getId(), userId));
        bridge.setOrganization(organization);
        return bridge;
    }

    @Nested
    @DisplayName("getAllInfo")
    class GetAllInfoTests {

        @Test
        @DisplayName("returns mapping points from the active organization")
        void getAllInfo_success() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(mappingPointRepository.findByOrganization_IdOrderByCreatedAtAscIdAsc(10L)).thenReturn(List.of(mappingPoint));

            List<MappingPointResponse> result = mappingPointService.getAllInfo(99L, 10L);

            assertEquals(1, result.size());
            assertEquals((short) 8, result.get(0).dots());
            assertEquals("Hvem enn som er i kassen ved gitt tidspunkt", result.get(0).responsibleForPoint());
        }

        @Test
        @DisplayName("rejects users outside the active organization")
        void getAllInfo_userOutsideOrg() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> mappingPointService.getAllInfo(99L, 10L));

            assertEquals("Organization not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("createMappingPoint")
    class CreateTests {

        @Test
        @DisplayName("creates a mapping point in the active organization")
        void create_success() {
            CreateMappingPointRequest request = new CreateMappingPointRequest(
                "AL § 1-5",
                (short) 8,
                "Salg eller utlevering til person som er under 18 år",
                "Mindreårige kunder bruker lånt/falskt ID-kort.",
                "Instruks om å sjekke legitimasjon.",
                "Hvem enn som er i kassen ved gitt tidspunkt"
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(mappingPointRepository.save(org.mockito.ArgumentMatchers.any(MappingPoint.class))).thenAnswer(invocation -> {
                MappingPoint saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 17L);
                return saved;
            });

            MappingPointResponse result = mappingPointService.createMappingPoint(request, 99L, 10L);

            assertEquals(17L, result.id());
            assertEquals("Hvem enn som er i kassen ved gitt tidspunkt", result.responsibleForPoint());
        }
    }

    @Nested
    @DisplayName("updateMappingPoint")
    class UpdateTests {

        @Test
        @DisplayName("updates only the provided fields")
        void update_success() {
            UpdateMappingPointRequest request = new UpdateMappingPointRequest(
                null,
                null,
                null,
                null,
                "Oppdatert tiltak",
                "Ny ansvarstekst"
            );

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(mappingPointRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(mappingPoint));
            when(mappingPointRepository.save(mappingPoint)).thenReturn(mappingPoint);

            MappingPointResponse result = mappingPointService.updateMappingPoint(17L, request, 99L, 10L);

            assertEquals("Oppdatert tiltak", result.measures());
            assertEquals("Ny ansvarstekst", result.responsibleForPoint());
            assertEquals("AL § 1-5", result.law());
        }
    }

    @Nested
    @DisplayName("deleteMappingPoint")
    class DeleteTests {

        @Test
        @DisplayName("deletes a mapping point in the active organization")
        void delete_success() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L)).thenReturn(Optional.of(orgMembership(99L)));
            when(mappingPointRepository.findByIdAndOrganization_Id(17L, 10L)).thenReturn(Optional.of(mappingPoint));

            mappingPointService.deleteMappingPoint(17L, 99L, 10L);

            verify(mappingPointRepository).delete(mappingPoint);
        }
    }
}
