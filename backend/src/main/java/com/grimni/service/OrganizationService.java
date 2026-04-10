package com.grimni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.grimni.domain.OrgDangerAnalysisCollaborator;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.AddOrganizationUserRequest;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.dto.UserOrgResponse;
import com.grimni.repository.OrgDangerAnalysisCollaboratorRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final OrgDangerAnalysisCollaboratorRepository dangerAnalysisCollaboratorRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrgUserBridgeRepository orgUserBridgeRepository,
                               OrgDangerAnalysisCollaboratorRepository dangerAnalysisCollaboratorRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.dangerAnalysisCollaboratorRepository = dangerAnalysisCollaboratorRepository;
        this.userRepository = userRepository;
    }

    public Organization createOrganization(CreateOrganizationRequest request, Long userId) {
        logger.info("Creating organization '{}' for user {}", request.orgName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Create organization failed: user {} not found", userId);
                    return new EntityNotFoundException("User not found");
                });

        Organization org = new Organization();
        org.setOrgName(request.orgName());
        org.setOrgAddress(request.orgAddress());
        org.setOrgNumber(request.orgNumber());
        org.setAlcoholEnabled(request.alcoholEnabled());
        org.setFoodEnabled(request.foodEnabled());
        org = organizationRepository.save(org);

        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(org.getId(), userId));
        bridge.setOrganization(org);
        bridge.setUser(user);
        bridge.setUserRole(OrgUserRole.OWNER);
        orgUserBridgeRepository.save(bridge);

        logger.info("Organization '{}' (id={}) created successfully, user {} assigned as OWNER", org.getOrgName(), org.getId(), userId);
        return org;
    }

    public List<Organization> findOrganizationsByUserId(Long userId) {
        logger.info("Fetching organizations for user {}", userId);
        List<Organization> orgs = orgUserBridgeRepository.findByUserId(userId).stream()
                .map(OrgUserBridge::getOrganization)
                .toList();
        logger.info("Found {} organizations for user {}", orgs.size(), userId);
        return orgs;
    }

    public Organization findOrganizationByIdAndUser(Long orgId, Long userId) {
        logger.info("Fetching organization {} for user {}", orgId, userId);
        OrgUserBridge bridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("Organization {} not found for user {}", orgId, userId);
                    return new EntityNotFoundException("Organization not found");
                });
        logger.info("Organization {} found for user {}", orgId, userId);
        return bridge.getOrganization();
    }

    public Organization findOrganizationById(Long orgId) {
        logger.info("Fetching organization {}", orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    logger.warn("Organization {} not found", orgId);
                    return new EntityNotFoundException("Organization not found");
                });
        logger.info("Organization {} found", orgId);
        return org;
    }

    public Organization updateOrganization(Long orgId, UpdateOrganizationRequest request, Long userId) {
        logger.info("Updating organization {} by user {}", orgId, userId);

        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("Update failed: organization {} not found for user {}", orgId, userId);
                    return new EntityNotFoundException("Organization not found");
                });

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    logger.warn("Update failed: organization {} not found", orgId);
                    return new EntityNotFoundException("Organization not found");
                });

        if (request.orgName() != null) org.setOrgName(request.orgName());
        if (request.orgAddress() != null) org.setOrgAddress(request.orgAddress());
        if (request.orgNumber() != null) org.setOrgNumber(request.orgNumber());
        if (request.alcoholEnabled() != null) org.setAlcoholEnabled(request.alcoholEnabled());
        if (request.foodEnabled() != null) org.setFoodEnabled(request.foodEnabled());

        org = organizationRepository.save(org);
        logger.info("Organization {} updated successfully by user {}", orgId, userId);
        return org;
    }

    public List<CollaboratorResponse> getDangerAnalysisCollaboratorsForOrg(long orgId) {
        List<CollaboratorResponse> collaborators = dangerAnalysisCollaboratorRepository.findByOrganizationId(orgId)
                .stream()
                .map(c -> CollaboratorResponse.fromEntity(c.getUser()))
                .toList();
        return collaborators;
    }

    public List<UserOrgResponse> getAllUsersInOrg(long orgId) {
        List<UserOrgResponse> response = orgUserBridgeRepository.findByOrganizationId(orgId)
        .stream()
        .map(u -> UserOrgResponse.fromEntity(u))
        .toList();

        return response;
    }

    public UserOrgResponse addUserToOrganization(Long orgId, Long requesterId, AddOrganizationUserRequest request) {
        logger.info("Adding user {} to organization {} with role {} by requester {}", request.userId(), orgId, request.orgRole(), requesterId);

        OrgUserBridge requesterBridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, requesterId)
                .orElseThrow(() -> {
                    logger.warn("Add org user failed: organization {} not found for requester {}", orgId, requesterId);
                    return new EntityNotFoundException("Organization not found");
                });

        if (requesterBridge.getUserRole() != OrgUserRole.OWNER) {
            logger.warn("Add org user denied: requester {} is not OWNER in organization {}", requesterId, orgId);
            throw new AccessDeniedException("Only owners can add users to the organization");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    logger.warn("Add org user failed: user {} not found", request.userId());
                    return new EntityNotFoundException("User not found");
                });

        if (orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, request.userId()).isPresent()) {
            logger.warn("Add org user failed: user {} already belongs to organization {}", request.userId(), orgId);
            throw new IllegalArgumentException("User is already in the organization");
        }

        Organization organization = requesterBridge.getOrganization();

        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(orgId, request.userId()));
        bridge.setOrganization(organization);
        bridge.setUser(user);
        bridge.setUserRole(request.orgRole());

        OrgUserBridge savedBridge = orgUserBridgeRepository.save(bridge);
        logger.info("User {} added to organization {} with role {}", request.userId(), orgId, request.orgRole());
        return UserOrgResponse.fromEntity(savedBridge);
    }
}
