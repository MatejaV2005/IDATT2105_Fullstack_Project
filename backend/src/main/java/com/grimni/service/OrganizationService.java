package com.grimni.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.Ccp;
import com.grimni.domain.CcpRecord;
import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.Certificate;
import com.grimni.domain.Course;
import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.Deviation;
import com.grimni.domain.FileObject;
import com.grimni.domain.InternalControlReview;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.MappingPoint;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.PrerequisiteCategory;
import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.PrerequisiteRoutineRecord;
import com.grimni.domain.PrerequisiteStandard;
import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.Todo;
import com.grimni.domain.User;
import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.enums.ResultStatus;
import com.grimni.domain.enums.ReviewStatus;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.enums.VerificationStatus;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.AddOrganizationUserRequest;
import com.grimni.dto.CollaboratorResponse;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.OrgAnalysisResponse;
import com.grimni.dto.OrgAnalysisResponse.CcpRecordStats;
import com.grimni.dto.OrgAnalysisResponse.DeviationCategoryStats;
import com.grimni.dto.OrgAnalysisResponse.DeviationStats;
import com.grimni.dto.OrgAnalysisResponse.PrerequisiteRoutineRecordStats;
import com.grimni.dto.OrgAnalysisResponse.ResourceStats;
import com.grimni.dto.OrgAnalysisResponse.UserStats;
import com.grimni.dto.MyOrganizationMembershipResponse;
import com.grimni.dto.TeamAssignmentsResponse;
import com.grimni.dto.TeamCourseProgressResponse;
import com.grimni.dto.TeamUserOverviewResponse;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.dto.UserDirectoryResponse;
import com.grimni.dto.UserOrgResponse;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
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
    private final CcpUserBridgeRepository ccpUserBridgeRepository;
    private final RoutineUserBridgeRepository routineUserBridgeRepository;
    private final CourseRepository courseRepository;
    private final CourseUserProgressRepository courseUserProgressRepository;
    private final DeviationRepository deviationRepository;
    private final MappingPointRepository mappingPointRepository;
    private final TodoRepository todoRepository;
    private final FileObjectRepository fileObjectRepository;
    private final CertificateRepository certificateRepository;
    private final CourseResponsibleUserRepository courseResponsibleUserRepository;
    private final CourseLinkRepository courseLinkRepository;
    private final FileCourseBridgeRepository fileCourseBridgeRepository;
    private final CcpRepository ccpRepository;
    private final CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository;
    private final CcpRecordRepository ccpRecordRepository;
    private final PrerequisiteCategoryRepository prerequisiteCategoryRepository;
    private final PrerequisiteStandardRepository prerequisiteStandardRepository;
    private final PrerequisiteRoutineRepository prerequisiteRoutineRepository;
    private final PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository;
    private final InternalControlReviewRepository internalControlReviewRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final IntervalRuleRepository intervalRuleRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrgUserBridgeRepository orgUserBridgeRepository,
                               OrgDangerAnalysisCollaboratorRepository dangerAnalysisCollaboratorRepository,
                               UserRepository userRepository,
                               CcpUserBridgeRepository ccpUserBridgeRepository,
                               RoutineUserBridgeRepository routineUserBridgeRepository,
                               CourseRepository courseRepository,
                               CourseUserProgressRepository courseUserProgressRepository,
                               DeviationRepository deviationRepository,
                               MappingPointRepository mappingPointRepository,
                               TodoRepository todoRepository,
                               FileObjectRepository fileObjectRepository,
                               CertificateRepository certificateRepository,
                               CourseResponsibleUserRepository courseResponsibleUserRepository,
                               CourseLinkRepository courseLinkRepository,
                               FileCourseBridgeRepository fileCourseBridgeRepository,
                               CcpRepository ccpRepository,
                               CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository,
                               CcpRecordRepository ccpRecordRepository,
                               PrerequisiteCategoryRepository prerequisiteCategoryRepository,
                               PrerequisiteStandardRepository prerequisiteStandardRepository,
                               PrerequisiteRoutineRepository prerequisiteRoutineRepository,
                               PrerequisiteRoutineRecordRepository prerequisiteRoutineRecordRepository,
                               InternalControlReviewRepository internalControlReviewRepository,
                               ProductCategoryRepository productCategoryRepository,
                               IntervalRuleRepository intervalRuleRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.dangerAnalysisCollaboratorRepository = dangerAnalysisCollaboratorRepository;
        this.userRepository = userRepository;
        this.ccpUserBridgeRepository = ccpUserBridgeRepository;
        this.routineUserBridgeRepository = routineUserBridgeRepository;
        this.courseRepository = courseRepository;
        this.courseUserProgressRepository = courseUserProgressRepository;
        this.deviationRepository = deviationRepository;
        this.mappingPointRepository = mappingPointRepository;
        this.todoRepository = todoRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.certificateRepository = certificateRepository;
        this.courseResponsibleUserRepository = courseResponsibleUserRepository;
        this.courseLinkRepository = courseLinkRepository;
        this.fileCourseBridgeRepository = fileCourseBridgeRepository;
        this.ccpRepository = ccpRepository;
        this.ccpCorrectiveMeasureRepository = ccpCorrectiveMeasureRepository;
        this.ccpRecordRepository = ccpRecordRepository;
        this.prerequisiteCategoryRepository = prerequisiteCategoryRepository;
        this.prerequisiteStandardRepository = prerequisiteStandardRepository;
        this.prerequisiteRoutineRepository = prerequisiteRoutineRepository;
        this.prerequisiteRoutineRecordRepository = prerequisiteRoutineRecordRepository;
        this.internalControlReviewRepository = internalControlReviewRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.intervalRuleRepository = intervalRuleRepository;
    }

    /**
     * Aggregates analysis data for the organization dashboard.
     */
    public OrgAnalysisResponse getOrgAnalysis(long orgId) {
        PrerequisiteRoutineRecordStats prerequisiteRoutineRecord = new PrerequisiteRoutineRecordStats(
            prerequisiteRoutineRecordRepository.countByOrganization_IdAndResultStatus(orgId, ResultStatus.COMPLETED),
            prerequisiteRoutineRecordRepository.countByOrganization_IdAndResultStatus(orgId, ResultStatus.FAILED)
        );

        CcpRecordStats ccpRecords = new CcpRecordStats(
            ccpRecordRepository.countByOrganization_IdAndVerificationStatus(orgId, VerificationStatus.SKIPPED),
            ccpRecordRepository.countByOrganization_IdAndVerificationStatus(orgId, VerificationStatus.VERIFIED),
            ccpRecordRepository.countByOrganization_IdAndVerificationStatus(orgId, VerificationStatus.REJECTED),
            ccpRecordRepository.countByOrganization_IdAndVerificationStatus(orgId, VerificationStatus.WAITING)
        );

        DeviationStats deviations = new DeviationStats(
            new DeviationCategoryStats(
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.IK_MAT, ReviewStatus.OPEN),
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.IK_MAT, ReviewStatus.CLOSED)
            ),
            new DeviationCategoryStats(
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.IK_ALKOHOL, ReviewStatus.OPEN),
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.IK_ALKOHOL, ReviewStatus.CLOSED)
            ),
            new DeviationCategoryStats(
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.OTHER, ReviewStatus.OPEN),
                deviationRepository.countByOrganization_IdAndCategoryAndReviewStatus(orgId, DeviationCategory.OTHER, ReviewStatus.CLOSED)
            )
        );

        List<OrgUserBridge> members = orgUserBridgeRepository.findByOrganizationId(orgId);
        long owners = members.stream().filter(bridge -> bridge.getUserRole() == OrgUserRole.OWNER).count();
        long managers = members.stream().filter(bridge -> bridge.getUserRole() == OrgUserRole.MANAGER).count();
        long workers = members.stream().filter(bridge -> bridge.getUserRole() == OrgUserRole.WORKER).count();
        UserStats users = new UserStats(owners, managers, workers);

        ResourceStats resources = new ResourceStats(
            prerequisiteRoutineRepository.countByOrganization_Id(orgId),
            ccpRepository.countByOrganization_Id(orgId),
            productCategoryRepository.countByOrganization_Id(orgId)
        );

        return new OrgAnalysisResponse(prerequisiteRoutineRecord, ccpRecords, deviations, users, resources);
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
        assertRequesterIsOwner(orgId, userId);

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

    @Transactional
    public void deleteOrganization(Long orgId, Long requesterId) {
        logger.info("Deleting organization {} by requester {}", orgId, requesterId);
        assertRequesterIsOwner(orgId, requesterId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        deleteIfAny(todoRepository.findByOrganization_Id(orgId), todoRepository::deleteAllInBatch);
        deleteIfAny(deviationRepository.findByOrganization_Id(orgId), deviationRepository::deleteAllInBatch);
        deleteIfAny(ccpRecordRepository.findByOrganization_Id(orgId), ccpRecordRepository::deleteAllInBatch);
        deleteIfAny(prerequisiteRoutineRecordRepository.findByOrganization_Id(orgId), prerequisiteRoutineRecordRepository::deleteAllInBatch);
        deleteIfAny(internalControlReviewRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId), internalControlReviewRepository::deleteAllInBatch);
        deleteIfAny(certificateRepository.findByOrganizationId(orgId), certificateRepository::deleteAllInBatch);

        List<Course> courses = courseRepository.findByOrganizationId(orgId);
        for (Course course : courses) {
            Long courseId = course.getId();
            deleteIfAny(courseUserProgressRepository.findByCourseId(courseId), courseUserProgressRepository::deleteAllInBatch);
            deleteIfAny(courseResponsibleUserRepository.findByCourseId(courseId), courseResponsibleUserRepository::deleteAllInBatch);
            deleteIfAny(courseLinkRepository.findByCourseId(courseId), courseLinkRepository::deleteAllInBatch);
            deleteIfAny(fileCourseBridgeRepository.findByCourseId(courseId), fileCourseBridgeRepository::deleteAllInBatch);
        }
        deleteIfAny(courses, courseRepository::deleteAllInBatch);

        deleteIfAny(fileObjectRepository.findByOrganization_Id(orgId), fileObjectRepository::deleteAllInBatch);
        deleteIfAny(mappingPointRepository.findByOrganization_IdOrderByCreatedAtAscIdAsc(orgId), mappingPointRepository::deleteAllInBatch);

        Set<Long> intervalIds = new HashSet<>();

        List<Ccp> ccps = ccpRepository.findByOrganization_Id(orgId);
        for (Ccp ccp : ccps) {
            collectIntervalId(intervalIds, ccp.getIntervalRule());
            ccpUserBridgeRepository.deleteByCcp_Id(ccp.getId());
            ccpCorrectiveMeasureRepository.deleteByCcp_Id(ccp.getId());
        }
        deleteIfAny(ccps, ccpRepository::deleteAllInBatch);

        List<PrerequisiteRoutine> routines = prerequisiteRoutineRepository.findByOrganization_Id(orgId);
        for (PrerequisiteRoutine routine : routines) {
            collectIntervalId(intervalIds, routine.getIntervalRule());
            routineUserBridgeRepository.deleteByRoutine_Id(routine.getId());
        }
        deleteIfAny(routines, prerequisiteRoutineRepository::deleteAllInBatch);
        deleteIfAny(prerequisiteStandardRepository.findByPrerequisiteCategory_Organization_Id(orgId), prerequisiteStandardRepository::deleteAllInBatch);
        deleteIfAny(prerequisiteCategoryRepository.findByOrganization_Id(orgId), prerequisiteCategoryRepository::deleteAllInBatch);

        deleteIfAny(productCategoryRepository.findByOrganization_Id(orgId), productCategoryRepository::deleteAllInBatch);
        if (!intervalIds.isEmpty()) {
            intervalRuleRepository.deleteAllById(intervalIds);
        }

        deleteIfAny(dangerAnalysisCollaboratorRepository.findByOrganizationId(orgId), dangerAnalysisCollaboratorRepository::deleteAllInBatch);
        deleteIfAny(orgUserBridgeRepository.findByOrganizationId(orgId), orgUserBridgeRepository::deleteAllInBatch);

        organizationRepository.delete(org);
        logger.info("Organization {} deleted successfully", orgId);
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

    public UserOrgResponse addUserToOrg(Long userId, OrgUserRole role, Long orgId, Long requesterId) {
        assertRequesterIsOwner(orgId, requesterId);
        return addUserToOrg(userId, role, orgId);
    }

    public UserOrgResponse updateUserRoleInOrg(Long userId, OrgUserRole role, Long orgId, Long requesterId) {
        assertRequesterIsOwner(orgId, requesterId);

        OrgUserBridge bridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member of this organization"));

        if (bridge.getUserRole() == OrgUserRole.OWNER && role != OrgUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot change role for an owner");
        }

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

    public void removeUserFromOrg(Long userId, Long orgId, Long requesterId) {
        assertRequesterIsOwner(orgId, requesterId);
        removeUserFromOrg(userId, orgId);
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

    public List<UserDirectoryResponse> getUserDirectory(long orgId) {
        Set<Long> existingMemberIds = orgUserBridgeRepository.findByOrganizationId(orgId)
                .stream()
                .map(bridge -> bridge.getUser().getId())
                .collect(java.util.stream.Collectors.toSet());

        return userRepository.findAll().stream()
                .filter(user -> !existingMemberIds.contains(user.getId()))
                .sorted(Comparator
                        .comparing(User::getLegalName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getEmail, String.CASE_INSENSITIVE_ORDER))
                .map(UserDirectoryResponse::fromEntity)
                .toList();
    }

    public List<TeamUserOverviewResponse> getTeamOverview(long orgId) {
        List<OrgUserBridge> members = orgUserBridgeRepository.findByOrganizationId(orgId).stream()
                .sorted(Comparator.comparing(
                        bridge -> bridge.getUser().getLegalName().toLowerCase(Locale.ROOT)))
                .toList();

        Map<Long, MutableTeamOverview> overviewByUserId = new HashMap<>();
        for (OrgUserBridge member : members) {
            overviewByUserId.put(member.getUser().getId(), new MutableTeamOverview(member));
        }

        for (CcpUserBridge bridge : ccpUserBridgeRepository.findAllByOrganizationIdWithUser(orgId)) {
            MutableTeamOverview overview = overviewByUserId.get(bridge.getUser().getId());
            if (overview != null) {
                overview.ccpAssignments.increment(bridge.getUserRole());
            }
        }

        for (RoutineUserBridge bridge : routineUserBridgeRepository.findAllByOrganizationIdWithUser(orgId)) {
            MutableTeamOverview overview = overviewByUserId.get(bridge.getUser().getId());
            if (overview != null) {
                overview.routineAssignments.increment(bridge.getUserRole());
            }
        }

        int totalCourses = courseRepository.findByOrganizationId(orgId).size();
        for (MutableTeamOverview overview : overviewByUserId.values()) {
            overview.courseProgressTotal = totalCourses;
        }

        for (CourseUserProgress progress : courseUserProgressRepository.findAllByOrganizationIdWithUser(orgId)) {
            if (!Boolean.TRUE.equals(progress.getIsCompleted())) {
                continue;
            }

            MutableTeamOverview overview = overviewByUserId.get(progress.getUser().getId());
            if (overview != null) {
                overview.courseProgressCompleted += 1;
            }
        }

        applyDeviationCounts(overviewByUserId, deviationRepository.countOpenCcpDeviationReviewsByReceiver(orgId), true);
        applyDeviationCounts(overviewByUserId, deviationRepository.countOpenRoutineDeviationReviewsByReceiver(orgId), false);

        List<MappingPoint> mappingPoints = mappingPointRepository.findByOrganization_IdOrderByCreatedAtAscIdAsc(orgId);
        for (MutableTeamOverview overview : overviewByUserId.values()) {
            String legalName = normalizeName(overview.legalName);
            overview.mappingPointResponsibilities = (int) mappingPoints.stream()
                    .map(MappingPoint::getResponsibleText)
                    .filter(text -> normalizeName(text).equals(legalName))
                    .count();
        }

        return members.stream()
                .map(member -> overviewByUserId.get(member.getUser().getId()))
                .filter(java.util.Objects::nonNull)
                .map(MutableTeamOverview::toResponse)
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

        assertRequesterIsOwner(orgId, requesterId);
        return addUserToOrg(request.userId(), request.orgRole(), orgId);
    }

    private void assertRequesterIsOwner(Long orgId, Long requesterId) {
        OrgUserBridge requesterBridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, requesterId)
                .orElseThrow(() -> {
                    logger.warn("Organization {} not found for requester {}", orgId, requesterId);
                    return new EntityNotFoundException("Organization not found");
                });

        if (requesterBridge.getUserRole() != OrgUserRole.OWNER) {
            logger.warn("Requester {} is not OWNER in organization {}", requesterId, orgId);
            throw new AccessDeniedException("Only owners can manage organization settings and members");
        }
    }

    private void applyDeviationCounts(
            Map<Long, MutableTeamOverview> overviewByUserId,
            List<Object[]> rows,
            boolean ccpCounts) {
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }

            Long userId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            MutableTeamOverview overview = overviewByUserId.get(userId);
            if (overview == null) {
                continue;
            }

            if (ccpCounts) {
                overview.openReviewedCcpDeviations = count;
            } else {
                overview.openReviewedRoutineDeviations = count;
            }
        }
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void collectIntervalId(Set<Long> intervalIds, IntervalRule intervalRule) {
        if (intervalRule != null && intervalRule.getId() != null) {
            intervalIds.add(intervalRule.getId());
        }
    }

    private <T> void deleteIfAny(List<T> items, java.util.function.Consumer<List<T>> deleter) {
        if (items != null && !items.isEmpty()) {
            deleter.accept(items);
        }
    }

    private static final class MutableAssignmentCounts {
        private int verifier;
        private int deviationReceiver;
        private int performer;
        private int deputy;

        private void increment(RoutineUserRole role) {
            if (role == null) {
                return;
            }

            switch (role) {
                case VERIFIER -> verifier += 1;
                case DEVIATION_RECEIVER -> deviationReceiver += 1;
                case PERFORMER -> performer += 1;
                case DEPUTY -> deputy += 1;
            }
        }

        private TeamAssignmentsResponse toResponse() {
            return new TeamAssignmentsResponse(verifier, deviationReceiver, performer, deputy);
        }
    }

    private static final class MutableTeamOverview {
        private final Long userId;
        private final String legalName;
        private final String orgRole;
        private final MutableAssignmentCounts ccpAssignments = new MutableAssignmentCounts();
        private final MutableAssignmentCounts routineAssignments = new MutableAssignmentCounts();
        private int mappingPointResponsibilities;
        private long openReviewedCcpDeviations;
        private long openReviewedRoutineDeviations;
        private int courseProgressCompleted;
        private int courseProgressTotal;

        private MutableTeamOverview(OrgUserBridge member) {
            this.userId = member.getUser().getId();
            this.legalName = member.getUser().getLegalName();
            this.orgRole = member.getUserRole().name();
        }

        private TeamUserOverviewResponse toResponse() {
            return new TeamUserOverviewResponse(
                userId,
                legalName,
                orgRole,
                ccpAssignments.toResponse(),
                routineAssignments.toResponse(),
                mappingPointResponsibilities,
                openReviewedCcpDeviations,
                openReviewedRoutineDeviations,
                new TeamCourseProgressResponse(courseProgressCompleted, courseProgressTotal)
            );
        }
    }
}
