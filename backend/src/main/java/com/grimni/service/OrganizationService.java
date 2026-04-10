package com.grimni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.dto.UserOrgResponse;
import com.grimni.repository.OrgDangerAnalysisCollaboratorRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing organizational entities and their administrative memberships.
 * <p>
 * This service serves as the core engine for multi-tenancy, providing functionality for:
 * <ul>
 * <li>Organization lifecycle management (Creation, retrieval, and updates).</li>
 * <li>Membership management through the {@link OrgUserBridge} linking users to organizations.</li>
 * <li>Role-based access control (RBAC) assignments (Owner, Manager, etc.).</li>
 * <li>Identification of specialized collaborators for regulatory danger analysis.</li>
 * </ul>
 */
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

    /**
     * Creates a new organization and establishes the creator as the initial OWNER.
     *
     * @param request the data containing organization details (name, number, address, etc.).
     * @param userId  the unique identifier of the user creating the organization.
     * @return the newly persisted {@link Organization} entity.
     * @throws EntityNotFoundException if the requesting user does not exist in the system.
     */
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

    /**
     * Retrieves all organizations where the specified user holds a membership.
     *
     * @param userId the ID of the user whose organizations are being queried.
     * @return a list of {@link Organization} entities.
     */
    public List<Organization> findOrganizationsByUserId(Long userId) {
        logger.info("Fetching organizations for user {}", userId);
        List<Organization> orgs = orgUserBridgeRepository.findByUserId(userId).stream()
                .map(OrgUserBridge::getOrganization)
                .toList();
        logger.info("Found {} organizations for user {}", orgs.size(), userId);
        return orgs;
    }

    /**
     * Fetches a specific organization after validating that the user is a member.
     *
     * @param orgId  the unique identifier of the organization.
     * @param userId the unique identifier of the user.
     * @return the {@link Organization} entity if membership is verified.
     * @throws EntityNotFoundException if the organization is not found or the user is not a member.
     */
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

    /**
     * Retrieves an organization by its primary identifier.
     *
     * @param orgId the unique identifier of the organization.
     * @return the {@link Organization} entity.
     * @throws EntityNotFoundException if the organization record does not exist.
     */
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

    /**
     * Updates an organization's configuration and contact details.
     * <p>
     * Validates that the performing user has administrative context within the organization
     * before applying non-null updates.
     *
     * @param orgId   the ID of the organization to update.
     * @param request the DTO containing updated organizational fields.
     * @param userId  the ID of the user performing the update.
     * @return the updated {@link Organization} entity.
     * @throws EntityNotFoundException if the organization or user membership is missing.
     */
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

    /**
     * Retrieves a specialized list of members qualified to perform danger analysis for the organization.
     *
     * @param orgId the unique identifier of the organization.
     * @return a list of {@link CollaboratorResponse} DTOs representing qualified users.
     */
    public List<CollaboratorResponse> getDangerAnalysisCollaboratorsForOrg(long orgId) {
        List<CollaboratorResponse> collaborators = dangerAnalysisCollaboratorRepository.findByOrganizationId(orgId)
                .stream()
                .map(c -> CollaboratorResponse.fromEntity(c.getUser()))
                .toList();
        return collaborators;
    }

    /**
     * Enrolls a user into an organization with a specific administrative role.
     *
     * @param userId the ID of the user to add.
     * @param role   the {@link OrgUserRole} to assign to the user.
     * @param orgId  the ID of the organization.
     * @return a {@link UserOrgResponse} representing the new membership bridge.
     * @throws EntityNotFoundException  if either the user or organization is missing.
     * @throws IllegalArgumentException if the user is already a member of the organization.
     */
    public UserOrgResponse addUserToOrg(Long userId, OrgUserRole role, Long orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this organization");
        }

        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(orgId, userId));
        bridge.setOrganization(org);
        bridge.setUser(user);
        bridge.setUserRole(role);
        orgUserBridgeRepository.save(bridge);

        return UserOrgResponse.fromEntity(bridge);
    }

    /**
     * Revokes a user's membership from an organization.
     * <p>
     * Logic enforces that users with the role of OWNER cannot be removed, ensuring
     * at least one primary administrator remains.
     *
     * @param userId the unique identifier of the user to remove.
     * @param orgId  the unique identifier of the organization.
     * @throws EntityNotFoundException  if the user is not a member of the organization.
     * @throws IllegalArgumentException if an attempt is made to remove an OWNER.
     */
    public void removeUserFromOrg(Long userId, Long orgId) {
        OrgUserBridge bridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member of this organization"));

        if (bridge.getUserRole() == OrgUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove an owner from the organization");
        }

        orgUserBridgeRepository.delete(bridge);
    }

    /**
     * Retrieves a list of all current members within a specific organization.
     *
     * @param orgId the unique identifier of the organization.
     * @return a list of {@link UserOrgResponse} DTOs mapping users to their organizational roles.
     */
    public List<UserOrgResponse> getAllUsersInOrg(long orgId) {
        List<UserOrgResponse> response = orgUserBridgeRepository.findByOrganizationId(orgId)
        .stream()
        .map(u -> UserOrgResponse.fromEntity(u))
        .toList();

        return response;
    }
}