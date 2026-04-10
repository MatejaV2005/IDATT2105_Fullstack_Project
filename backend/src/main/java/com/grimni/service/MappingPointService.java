package com.grimni.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.MappingPoint;
import com.grimni.domain.OrgUserBridge;
import com.grimni.dto.CreateMappingPointRequest;
import com.grimni.dto.MappingPointResponse;
import com.grimni.dto.UpdateMappingPointRequest;
import com.grimni.repository.MappingPointRepository;
import com.grimni.repository.OrgUserBridgeRepository;

/**
 * Service class for managing Mapping Points (kartleggingspunkter) within the HACCP hazard analysis framework.
 * <p>
 * This service facilitates the identification and documentation of specific process steps where
 * hazards may occur. It manages the association between legal requirements, severity assessments
 * (dots), specific challenges, and the mitigating measures required to maintain food safety.
 * </p>
 */
@Service
public class MappingPointService {

    private final MappingPointRepository mappingPointRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;

    public MappingPointService(
            MappingPointRepository mappingPointRepository,
            OrgUserBridgeRepository orgUserBridgeRepository) {
        this.mappingPointRepository = mappingPointRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
    }

    /**
     * Retrieves all mapping points for a specific organization, ordered by creation date and ID.
     *
     * @param authenticatedUserId the ID of the user requesting the data.
     * @param orgId the organization ID scope.
     * @return a list of {@link MappingPointResponse} DTOs representing the organization's hazard mapping.
     * @throws RuntimeException if the user does not belong to the organization.
     */
    @Transactional(readOnly = true)
    public List<MappingPointResponse> getAllInfo(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        return mappingPointRepository.findByOrganization_IdOrderByCreatedAtAscIdAsc(orgId).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Creates and persists a new Mapping Point entry.
     *
     * @param request the DTO containing legal references, severity, title, challenges, and measures.
     * @param authenticatedUserId the ID of the user performing the creation.
     * @param orgId the organization ID to which this point will belong.
     * @return a {@link MappingPointResponse} representing the newly created entity.
     * @throws RuntimeException if the organization is not found or user membership validation fails.
     */
    @Transactional
    public MappingPointResponse createMappingPoint(
            CreateMappingPointRequest request,
            Long authenticatedUserId,
            Long orgId) {
        OrgUserBridge membership = ensureAuthenticatedMember(authenticatedUserId, orgId);

        MappingPoint mappingPoint = new MappingPoint();
        mappingPoint.setLaw(request.law());
        mappingPoint.setSeverityDots(request.dots());
        mappingPoint.setTitle(request.title());
        mappingPoint.setChallenges(request.challenges());
        mappingPoint.setMeasures(request.measures());
        mappingPoint.setResponsibleText(request.responsibleText());
        mappingPoint.setOrganization(membership.getOrganization());

        return toResponse(mappingPointRepository.save(mappingPoint));
    }

    /**
     * Updates specific fields of an existing Mapping Point.
     * <p>
     * Only non-null fields in the request DTO will be applied to the existing entity.
     * </p>
     *
     * @param mappingPointId the unique ID of the Mapping Point to update.
     * @param request the DTO containing updated field values.
     * @param authenticatedUserId the ID of the user performing the update.
     * @param orgId the organization ID for scope validation.
     * @return the updated {@link MappingPointResponse}.
     * @throws RuntimeException if the mapping point is not found within the specified organization.
     */
    @Transactional
    public MappingPointResponse updateMappingPoint(
            Long mappingPointId,
            UpdateMappingPointRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        MappingPoint mappingPoint = getMappingPointInOrg(mappingPointId, orgId);

        if (request.law() != null) {
            mappingPoint.setLaw(request.law());
        }
        if (request.dots() != null) {
            mappingPoint.setSeverityDots(request.dots());
        }
        if (request.title() != null) {
            mappingPoint.setTitle(request.title());
        }
        if (request.challenges() != null) {
            mappingPoint.setChallenges(request.challenges());
        }
        if (request.measures() != null) {
            mappingPoint.setMeasures(request.measures());
        }
        if (request.responsibleText() != null) {
            mappingPoint.setResponsibleText(request.responsibleText());
        }

        return toResponse(mappingPointRepository.save(mappingPoint));
    }

    /**
     * Deletes a Mapping Point from the system.
     *
     * @param mappingPointId the ID of the Mapping Point to remove.
     * @param authenticatedUserId the ID of the user performing the deletion.
     * @param orgId the organization ID for scope validation.
     * @throws RuntimeException if the mapping point is not found or membership is invalid.
     */
    @Transactional
    public void deleteMappingPoint(Long mappingPointId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        mappingPointRepository.delete(getMappingPointInOrg(mappingPointId, orgId));
    }

    /**
     * Validates that the user holds a valid membership within the target organization.
     */
    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    /**
     * Retrieves a Mapping Point by ID while enforcing organization-based access control.
     */
    private MappingPoint getMappingPointInOrg(Long mappingPointId, Long orgId) {
        return mappingPointRepository.findByIdAndOrganization_Id(mappingPointId, orgId)
            .orElseThrow(() -> new RuntimeException("Mapping point not found"));
    }

    /**
     * Maps the internal domain entity to a public-facing response DTO.
     */
    private MappingPointResponse toResponse(MappingPoint mappingPoint) {
        return new MappingPointResponse(
            mappingPoint.getId(),
            mappingPoint.getLaw(),
            mappingPoint.getSeverityDots(),
            mappingPoint.getTitle(),
            mappingPoint.getChallenges(),
            mappingPoint.getMeasures(),
            mappingPoint.getResponsibleText()
        );
    }
}