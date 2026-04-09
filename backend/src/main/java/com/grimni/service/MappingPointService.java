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

    @Transactional(readOnly = true)
    public List<MappingPointResponse> getAllInfo(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        return mappingPointRepository.findByOrganization_IdOrderByCreatedAtAscIdAsc(orgId).stream()
            .map(this::toResponse)
            .toList();
    }

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

    @Transactional
    public void deleteMappingPoint(Long mappingPointId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        mappingPointRepository.delete(getMappingPointInOrg(mappingPointId, orgId));
    }

    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private MappingPoint getMappingPointInOrg(Long mappingPointId, Long orgId) {
        return mappingPointRepository.findByIdAndOrganization_Id(mappingPointId, orgId)
            .orElseThrow(() -> new RuntimeException("Mapping point not found"));
    }

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
