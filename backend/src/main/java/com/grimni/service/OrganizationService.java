package com.grimni.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.AddOrganizationUserRequest;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.MyOrganizationMembershipResponse;
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
 * <li>Multi-tenant session resolution and organization switching logic.</li>
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
     * @return a list of {@link Organization} entities sorted by ID.
     */
    public List<Organization> findOrganizationsByUserId(Long userId) {
        logger.info("Fetching organizations for user {}", userId);
        List<Organization> orgs = orgUserBridgeRepository.findByUserId(userId).stream()
                .sorted((left, right) -> Long.compare(left.getOrganization().getId(), right.getOrganization().getId()))
                .map(OrgUserBridge::getOrganization)
                .toList();
        logger.info("Found {} organizations for user {}", orgs.size(), userId);
        return orgs;
    }

    /**
     * Retrieves all membership bridge entities for a user.
     *
     * @param userId the unique ID of the user.
     * @return a list of {@link OrgUserBridge} entities sorted by organization ID.
     */
    public List<OrgUserBridge> findMembershipsByUserId(Long userId) {
        return orgUserBridgeRepository.findByUserId(userId).stream()
                .sorted((left, right) -> Long.compare(left.getOrganization().getId(), right.getOrganization().getId()))
                .toList();
    }

    /**
     * Provides the primary (first available) membership for a user.
     *
     * @param userId the unique ID of the user.
     * @return an Optional containing the first {@link OrgUserBridge} found.
     */
    public Optional<OrgUserBridge> findDefaultMembershipForUser(Long userId) {
        return findMembershipsByUserId(userId).stream().findFirst();
    }

    /**
     * Retrieves a specific membership for a user in a target organization.
     *
     * @param userId the ID of the user.
     * @param orgId  the ID of the organization.
     * @return the {@link OrgUserBridge} record.
     * @throws AccessDeniedException if the user is not a member of the organization.
     */
    public OrgUserBridge getMembershipForUser(Long userId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to the selected organization"));
    }

    /**
     * Resolves which organization context to use during a token refresh.
     * <p>
     * Logic defaults to the first available membership if no specific organization is requested 
     * or if the requested organization is no longer accessible.
     *
     * @param userId         the unique identifier of the user.
     * @param requestedOrgId the ID of the organization context requested by the client.
     * @return the resolved {@link OrgUserBridge}.
     * @throws org.springframework.security.authentication.BadCredentialsException if the user has no memberships.
     */
    public OrgUserBridge resolveRefreshMembership(Long userId, Long requestedOrgId) {
        List<OrgUserBridge> memberships = findMembershipsByUserId(userId);
        if (memberships.isEmpty()) {
            throw new org.springframework.security.authentication.BadCredentialsException("No accessible organizations for this session");
        }

        if (requestedOrgId == null) {
            return memberships.getFirst();
        }

        return memberships.stream()
                .filter(bridge -> bridge.getOrganization().getId().equals(requestedOrgId))
                .findFirst()
                .orElse(memberships.getFirst());
    }

    /**
     * Returns a specialized response list of all user memberships, highlighting the current active context.
     *
     * @param userId       the requesting user's ID.
     * @param currentOrgId the ID of the organization currently active in the session.
     * @return a list of {@link MyOrganizationMembershipResponse} DTOs.
     */
    public List<MyOrganizationMembershipResponse> getMyOrganizationMemberships(Long userId, Long currentOrgId) {
        return findMembershipsByUserId(userId).stream()
                .map(bridge -> MyOrganizationMembershipResponse.fromEntity(bridge, currentOrgId))
                .toList();
    }

    /**
     * Validates access and returns an organization for a specific user.
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
     *
     * @param orgId   the ID of the organization to update.
     * @param request the DTO containing updated organizational fields.
     * @param userId  the ID of the user performing the update.
     * @return the updated {@link Organization} entity.
     */
    public Organization updateOrganization(Long orgId, UpdateOrganizationRequest request, Long userId) {
        logger.info("Updating organization {} by user {}", orgId, userId);

        // Security check
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
        return dangerAnalysisCollaboratorRepository.findByOrganizationId(orgId)
                .stream()
                .map(c -> CollaboratorResponse.fromEntity(c.getUser()))
                .toList();
    }

    /**
     * Enrolls a user into an organization with a specific administrative role.
     *
     * @param userId the ID of the user to add.
     * @param role   the {@link OrgUserRole} to assign to the user.
     * @param orgId  the ID of the organization.
     * @return a {@link UserOrgResponse} representing the new membership bridge.
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
     *
     * @param userId the unique identifier of the user to remove.
     * @param orgId  the unique identifier of the organization.
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
        return orgUserBridgeRepository.findByOrganizationId(orgId)
                .stream()
                .map(UserOrgResponse::fromEntity)
                .toList();
    }

    /**
     * Adds a user to an organization context, validating that the requester is an OWNER.
     *
     * @param orgId       the target organization ID.
     * @param requesterId the user ID of the person performing the addition.
     * @param request     the DTO containing the target user ID and intended role.
     * @return a {@link UserOrgResponse} representing the new membership.
     * @throws AccessDeniedException if the requester is not an OWNER.
     */
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

        // Reuse the validated logic from the internal method
        return addUserToOrg(request.userId(), request.orgRole(), orgId);
    }
}