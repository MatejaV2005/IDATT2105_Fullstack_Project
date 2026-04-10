package com.grimni.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.PrerequisiteCategory;
import com.grimni.domain.PrerequisiteRoutine;
import com.grimni.domain.PrerequisiteStandard;
import com.grimni.domain.RoutineUserBridge;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.RoutineUserBridgeId;
import com.grimni.dto.CreatePrerequisiteCategoryRequest;
import com.grimni.dto.CreatePrerequisiteRoutineRequest;
import com.grimni.dto.CreatePrerequisiteStandardRequest;
import com.grimni.dto.PrerequisiteCategoryAllInfoResponse;
import com.grimni.dto.PrerequisiteCategoryResponse;
import com.grimni.dto.PrerequisiteIntervalResponse;
import com.grimni.dto.PrerequisitePointResponse;
import com.grimni.dto.PrerequisiteUserResponse;
import com.grimni.dto.ReplaceRoutineAssignmentsRequest;
import com.grimni.dto.RoutinePrerequisitePointResponse;
import com.grimni.dto.StandardPrerequisitePointResponse;
import com.grimni.dto.UpdatePrerequisiteCategoryRequest;
import com.grimni.dto.UpdatePrerequisiteRoutineRequest;
import com.grimni.dto.UpdatePrerequisiteStandardRequest;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.IntervalRuleRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteCategoryRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.PrerequisiteStandardRepository;
import com.grimni.repository.RoutineUserBridgeRepository;
import com.grimni.repository.UserRepository;

/**
 * Service for managing the Prerequisite Program (PRP) infrastructure within the HACCP framework.
 * <p>
 * This service handles the hierarchical structure of food safety compliance, including:
 * <ul>
 * <li><b>Categories:</b> High-level groupings of compliance areas.</li>
 * <li><b>Standards:</b> Static requirements and documentation points.</li>
 * <li><b>Routines:</b> Recurring operational tasks with associated intervals and personnel assignments.</li>
 * </ul>
 */
@Service
public class PrerequisiteCategoryService {

    private static final DateTimeFormatter REPEAT_TEXT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PrerequisiteCategoryRepository prerequisiteCategoryRepository;
    private final PrerequisiteStandardRepository prerequisiteStandardRepository;
    private final PrerequisiteRoutineRepository prerequisiteRoutineRepository;
    private final RoutineUserBridgeRepository routineUserBridgeRepository;
    private final IntervalRuleRepository intervalRuleRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final UserRepository userRepository;
    private final CcpRepository ccpRepository;

    public PrerequisiteCategoryService(
            PrerequisiteCategoryRepository prerequisiteCategoryRepository,
            PrerequisiteStandardRepository prerequisiteStandardRepository,
            PrerequisiteRoutineRepository prerequisiteRoutineRepository,
            RoutineUserBridgeRepository routineUserBridgeRepository,
            IntervalRuleRepository intervalRuleRepository,
            OrgUserBridgeRepository orgUserBridgeRepository,
            UserRepository userRepository,
            CcpRepository ccpRepository) {
        this.prerequisiteCategoryRepository = prerequisiteCategoryRepository;
        this.prerequisiteStandardRepository = prerequisiteStandardRepository;
        this.prerequisiteRoutineRepository = prerequisiteRoutineRepository;
        this.routineUserBridgeRepository = routineUserBridgeRepository;
        this.intervalRuleRepository = intervalRuleRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.userRepository = userRepository;
        this.ccpRepository = ccpRepository;
    }

    /**
     * Retrieves the complete PRP hierarchy for an organization, including all categories and nested points.
     * <p>
     * Points (standards and routines) are unified into a single chronological stream per category
     * and enriched with user assignment metadata and interval descriptions.
     *
     * @param authenticatedUserId the unique identifier of the requesting user.
     * @param orgId the unique identifier of the organization.
     * @return a list of {@link PrerequisiteCategoryAllInfoResponse} containing the full PRP tree.
     * @throws RuntimeException if user membership validation fails.
     */
    @Transactional(readOnly = true)
    public List<PrerequisiteCategoryAllInfoResponse> getAllInfo(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        List<PrerequisiteCategory> categories = new ArrayList<>(prerequisiteCategoryRepository.findByOrganization_Id(orgId));
        List<PrerequisiteStandard> standards = new ArrayList<>(prerequisiteStandardRepository.findByPrerequisiteCategory_Organization_Id(orgId));
        List<PrerequisiteRoutine> routines = new ArrayList<>(prerequisiteRoutineRepository.findByOrganization_Id(orgId));

        categories.sort(this::compareCategory);
        standards.sort(this::compareStandard);
        routines.sort(this::compareRoutine);

        Map<Long, List<PointEnvelope>> pointsByCategoryId = new java.util.HashMap<>();
        Map<Long, List<RoutineUserBridge>> routineBridgesByRoutineId = loadRoutineBridgesByRoutineId(
            routines.stream().map(PrerequisiteRoutine::getId).toList()
        );

        for (PrerequisiteStandard standard : standards) {
            Long categoryId = standard.getPrerequisiteCategory().getId();
            pointsByCategoryId
                .computeIfAbsent(categoryId, ignored -> new ArrayList<>())
                .add(new PointEnvelope(
                    standard.getCreatedAt(),
                    standard.getId(),
                    toStandardResponse(standard)
                ));
        }

        for (PrerequisiteRoutine routine : routines) {
            Long categoryId = routine.getPrerequisiteCategory().getId();
            pointsByCategoryId
                .computeIfAbsent(categoryId, ignored -> new ArrayList<>())
                .add(new PointEnvelope(
                    routine.getCreatedAt(),
                    routine.getId(),
                    toRoutineResponse(routine, routineBridgesByRoutineId.getOrDefault(routine.getId(), List.of()))
                ));
        }

        return categories.stream()
            .map(category -> {
                List<PrerequisitePointResponse> points = pointsByCategoryId
                    .getOrDefault(category.getId(), List.of())
                    .stream()
                    .sorted(this::comparePointEnvelope)
                    .map(PointEnvelope::point)
                    .toList();

                return new PrerequisiteCategoryAllInfoResponse(
                    category.getId(),
                    category.getCategoryName(),
                    points
                );
            })
            .toList();
    }

    /**
     * Initializes a new PRP category for the organization.
     *
     * @param request the DTO containing the category name.
     * @param authenticatedUserId the creator's user ID.
     * @param orgId the target organization ID.
     * @return the created {@link PrerequisiteCategoryResponse}.
     */
    @Transactional
    public PrerequisiteCategoryResponse createCategory(
            CreatePrerequisiteCategoryRequest request,
            Long authenticatedUserId,
            Long orgId) {
        OrgUserBridge memberBridge = ensureAuthenticatedMember(authenticatedUserId, orgId);

        PrerequisiteCategory category = new PrerequisiteCategory();
        category.setCategoryName(request.categoryName());
        category.setOrganization(memberBridge.getOrganization());

        PrerequisiteCategory savedCategory = prerequisiteCategoryRepository.save(category);
        return new PrerequisiteCategoryResponse(savedCategory.getId(), savedCategory.getCategoryName());
    }

    /**
     * Updates the metadata of an existing PRP category.
     *
     * @param categoryId unique ID of the category.
     * @param request the DTO containing updated fields.
     * @param authenticatedUserId updater's user ID.
     * @param orgId organization scope.
     * @return the updated {@link PrerequisiteCategoryResponse}.
     */
    @Transactional
    public PrerequisiteCategoryResponse updateCategory(
            Long categoryId,
            UpdatePrerequisiteCategoryRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteCategory category = getCategoryInOrg(categoryId, orgId);

        if (request.categoryName() != null) {
            category.setCategoryName(request.categoryName());
        }

        PrerequisiteCategory savedCategory = prerequisiteCategoryRepository.save(category);
        return new PrerequisiteCategoryResponse(savedCategory.getId(), savedCategory.getCategoryName());
    }

    /**
     * Permanently deletes a PRP category and all its associated routines and standards.
     *
     * @param categoryId ID of the category to remove.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteCategory category = getCategoryInOrg(categoryId, orgId);

        for (PrerequisiteRoutine routine : new ArrayList<>(category.getRoutines())) {
            deleteRoutineEntity(routine);
        }

        prerequisiteStandardRepository.deleteAll(new ArrayList<>(category.getStandards()));
        prerequisiteCategoryRepository.delete(category);
    }

    /**
     * Adds a static prerequisite standard to a category.
     *
     * @param categoryId the parent category ID.
     * @param request the standard details.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     * @return created {@link StandardPrerequisitePointResponse}.
     */
    @Transactional
    public StandardPrerequisitePointResponse createStandard(
            Long categoryId,
            CreatePrerequisiteStandardRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteCategory category = getCategoryInOrg(categoryId, orgId);

        PrerequisiteStandard standard = new PrerequisiteStandard();
        standard.setStandardName(request.title());
        standard.setStandardDescription(request.description());
        standard.setPrerequisiteCategory(category);

        return toStandardResponse(prerequisiteStandardRepository.save(standard));
    }

    /**
     * Modifies an existing prerequisite standard.
     *
     * @param standardId ID of standard to update.
     * @param request update data.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     * @return updated {@link StandardPrerequisitePointResponse}.
     */
    @Transactional
    public StandardPrerequisitePointResponse updateStandard(
            Long standardId,
            UpdatePrerequisiteStandardRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteStandard standard = getStandardInOrg(standardId, orgId);

        if (request.title() != null) {
            standard.setStandardName(request.title());
        }
        if (request.description() != null) {
            standard.setStandardDescription(request.description());
        }
        if (request.categoryId() != null) {
            standard.setPrerequisiteCategory(getCategoryInOrg(request.categoryId(), orgId));
        }

        return toStandardResponse(prerequisiteStandardRepository.save(standard));
    }

    /**
     * Removes a prerequisite standard.
     *
     * @param standardId ID of standard.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     */
    @Transactional
    public void deleteStandard(Long standardId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        prerequisiteStandardRepository.delete(getStandardInOrg(standardId, orgId));
    }

    /**
     * Registers a new recurring routine with temporal rules and user assignments.
     *
     * @param categoryId the parent category.
     * @param request routine specifications including interval and roles.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     * @return created {@link RoutinePrerequisitePointResponse}.
     */
    @Transactional
    public RoutinePrerequisitePointResponse createRoutine(
            Long categoryId,
            CreatePrerequisiteRoutineRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteCategory category = getCategoryInOrg(categoryId, orgId);

        IntervalRule intervalRule = new IntervalRule();
        intervalRule.setIntervalStart(request.intervalStart());
        intervalRule.setIntervalRepeatTime(request.intervalRepeatTime());
        intervalRule = intervalRuleRepository.save(intervalRule);

        PrerequisiteRoutine routine = new PrerequisiteRoutine();
        routine.setTitle(request.title());
        routine.setDescription(request.description());
        routine.setImmediateCorrectiveAction(request.measures());
        routine.setOrganization(category.getOrganization());
        routine.setPrerequisiteCategory(category);
        routine.setIntervalRule(intervalRule);
        routine = prerequisiteRoutineRepository.save(routine);

        List<RoutineUserBridge> savedAssignments = replaceAssignmentsInternal(
            routine,
            request.verifierUserIds(),
            request.deviationReceiverUserIds(),
            request.performerUserIds(),
            request.deputyUserIds()
        );

        return toRoutineResponse(routine, savedAssignments);
    }

    /**
     * Updates an existing routine's configuration, temporal rule, or performer/deputy roles.
     *
     * @param routineId unique ID of the routine.
     * @param request updated data.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     * @return updated {@link RoutinePrerequisitePointResponse}.
     */
    @Transactional
    public RoutinePrerequisitePointResponse updateRoutine(
            Long routineId,
            UpdatePrerequisiteRoutineRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteRoutine routine = getRoutineInOrg(routineId, orgId);

        if (request.title() != null) {
            routine.setTitle(request.title());
        }
        if (request.description() != null) {
            routine.setDescription(request.description());
        }
        if (request.measures() != null) {
            routine.setImmediateCorrectiveAction(request.measures());
        }
        if (request.categoryId() != null) {
            routine.setPrerequisiteCategory(getCategoryInOrg(request.categoryId(), orgId));
        }
        if (request.intervalStart() != null || request.intervalRepeatTime() != null) {
            IntervalRule intervalRule = routine.getIntervalRule();
            if (intervalRule == null) {
                intervalRule = new IntervalRule();
            }
            if (request.intervalStart() != null) {
                intervalRule.setIntervalStart(request.intervalStart());
            }
            if (request.intervalRepeatTime() != null) {
                intervalRule.setIntervalRepeatTime(request.intervalRepeatTime());
            }
            intervalRule = intervalRuleRepository.save(intervalRule);
            routine.setIntervalRule(intervalRule);
        }

        PrerequisiteRoutine savedRoutine = prerequisiteRoutineRepository.save(routine);

        if (request.performers() != null) {
            replaceRoleAssignments(savedRoutine, RoutineUserRole.PERFORMER, request.performers());
        }
        if (request.deputy() != null) {
            replaceRoleAssignments(savedRoutine, RoutineUserRole.DEPUTY, request.deputy());
        }

        return toRoutineResponse(savedRoutine, loadRoutineBridges(savedRoutine.getId()));
    }

    /**
     * Deletes a routine and its associated bridges, plus orphaned interval rules.
     *
     * @param routineId ID of routine.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     */
    @Transactional
    public void deleteRoutine(Long routineId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        deleteRoutineEntity(getRoutineInOrg(routineId, orgId));
    }

    /**
     * Overwrites all role-based assignments (Verifiers, Performers, etc.) for a routine.
     *
     * @param routineId unique ID of the target routine.
     * @param request collection of user IDs per role.
     * @param authenticatedUserId user ID.
     * @param orgId organization scope.
     * @return the updated routine DTO with new assignments.
     */
    @Transactional
    public RoutinePrerequisitePointResponse replaceRoutineAssignments(
            Long routineId,
            ReplaceRoutineAssignmentsRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        PrerequisiteRoutine routine = getRoutineInOrg(routineId, orgId);

        List<RoutineUserBridge> savedAssignments = replaceAssignmentsInternal(
            routine,
            request.verifierUserIds(),
            request.deviationReceiverUserIds(),
            request.performerUserIds(),
            request.deputyUserIds()
        );

        return toRoutineResponse(routine, savedAssignments);
    }

    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private PrerequisiteCategory getCategoryInOrg(Long categoryId, Long orgId) {
        return prerequisiteCategoryRepository.findByIdAndOrganization_Id(categoryId, orgId)
            .orElseThrow(() -> new RuntimeException("Prerequisite category not found"));
    }

    private PrerequisiteStandard getStandardInOrg(Long standardId, Long orgId) {
        return prerequisiteStandardRepository.findByIdAndPrerequisiteCategory_Organization_Id(standardId, orgId)
            .orElseThrow(() -> new RuntimeException("Prerequisite standard not found"));
    }

    private PrerequisiteRoutine getRoutineInOrg(Long routineId, Long orgId) {
        return prerequisiteRoutineRepository.findByIdAndOrganization_Id(routineId, orgId)
            .orElseThrow(() -> new RuntimeException("Prerequisite routine not found"));
    }

    /**
     * Internal logic for handling assignment replacement across multiple roles.
     */
    private List<RoutineUserBridge> replaceAssignmentsInternal(
            PrerequisiteRoutine routine,
            List<Long> verifierUserIds,
            List<Long> deviationReceiverUserIds,
            List<Long> performerUserIds,
            List<Long> deputyUserIds) {
        Map<RoutineUserRole, LinkedHashSet<Long>> requestedAssignments = new EnumMap<>(RoutineUserRole.class);
        requestedAssignments.put(RoutineUserRole.VERIFIER, toOrderedSet(verifierUserIds));
        requestedAssignments.put(RoutineUserRole.DEVIATION_RECEIVER, toOrderedSet(deviationReceiverUserIds));
        requestedAssignments.put(RoutineUserRole.PERFORMER, toOrderedSet(performerUserIds));
        requestedAssignments.put(RoutineUserRole.DEPUTY, toOrderedSet(deputyUserIds));

        Set<Long> allUserIds = requestedAssignments.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, User> usersById = loadUsersInOrganization(allUserIds, routine.getOrganization().getId());

        routineUserBridgeRepository.deleteByRoutine_Id(routine.getId());

        List<RoutineUserBridge> newBridges = new ArrayList<>();
        for (Map.Entry<RoutineUserRole, LinkedHashSet<Long>> entry : requestedAssignments.entrySet()) {
            for (Long userId : entry.getValue()) {
                RoutineUserBridge bridge = new RoutineUserBridge();
                bridge.setId(new RoutineUserBridgeId(userId, routine.getId(), entry.getKey()));
                bridge.setRoutine(routine);
                bridge.setUser(usersById.get(userId));
                bridge.setUserRole(entry.getKey());
                newBridges.add(bridge);
            }
        }

        if (newBridges.isEmpty()) {
            return List.of();
        }

        return routineUserBridgeRepository.saveAll(newBridges);
    }

    private Map<Long, User> loadUsersInOrganization(Set<Long> userIds, Long orgId) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("All assigned users must exist");
        }

        Set<Long> userIdsInOrganization = orgUserBridgeRepository.findByOrganizationIdAndUserIdIn(orgId, userIds)
            .stream()
            .map(bridge -> bridge.getId().getUserId())
            .collect(Collectors.toSet());

        if (userIdsInOrganization.size() != userIds.size()) {
            throw new IllegalArgumentException("Assigned users must belong to the active organization");
        }

        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private List<RoutineUserBridge> loadRoutineBridges(Long routineId) {
        if (routineId == null) {
            return List.of();
        }

        return routineUserBridgeRepository.findAllWithUserByRoutineIds(List.of(routineId));
    }

    private Map<Long, List<RoutineUserBridge>> loadRoutineBridgesByRoutineId(List<Long> routineIds) {
        if (routineIds.isEmpty()) {
            return Map.of();
        }

        return routineUserBridgeRepository.findAllWithUserByRoutineIds(routineIds).stream()
            .collect(Collectors.groupingBy(bridge -> bridge.getRoutine().getId()));
    }

    /**
     * Executes physical deletion of a routine and handles related temporal cleanup.
     */
    private void deleteRoutineEntity(PrerequisiteRoutine routine) {
        Long intervalId = routine.getIntervalRule() != null ? routine.getIntervalRule().getId() : null;

        routineUserBridgeRepository.deleteByRoutine_Id(routine.getId());
        prerequisiteRoutineRepository.delete(routine);
        prerequisiteRoutineRepository.flush();
        cleanupIntervalIfOrphaned(intervalId);
    }

    /**
     * Replaces assignments for a specific singular role.
     */
    private void replaceRoleAssignments(PrerequisiteRoutine routine, RoutineUserRole role, List<Long> userIds) {
        routineUserBridgeRepository.deleteByRoutine_IdAndId_UserRole(routine.getId(), role);

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<Long> userIdSet = new LinkedHashSet<>(userIds);
        Map<Long, User> usersById = loadUsersInOrganization(userIdSet, routine.getOrganization().getId());

        List<RoutineUserBridge> bridges = new ArrayList<>();
        for (Long userId : userIdSet) {
            RoutineUserBridge bridge = new RoutineUserBridge();
            bridge.setId(new RoutineUserBridgeId(userId, routine.getId(), role));
            bridge.setRoutine(routine);
            bridge.setUser(usersById.get(userId));
            bridge.setUserRole(role);
            bridges.add(bridge);
        }
        routineUserBridgeRepository.saveAll(bridges);
    }

    /**
     * Removes an IntervalRule if no longer referenced by any Routine or CCP.
     */
    private void cleanupIntervalIfOrphaned(Long intervalId) {
        if (intervalId == null) {
            return;
        }

        boolean usedByRoutine = prerequisiteRoutineRepository.existsByIntervalRule_Id(intervalId);
        boolean usedByCcp = ccpRepository.existsByIntervalRule_Id(intervalId);

        if (!usedByRoutine && !usedByCcp) {
            intervalRuleRepository.deleteById(intervalId);
        }
    }

    private PrerequisiteCategoryAllInfoResponse toCategoryAllInfoResponse(
            PrerequisiteCategory category,
            List<PrerequisitePointResponse> points) {
        return new PrerequisiteCategoryAllInfoResponse(category.getId(), category.getCategoryName(), points);
    }

    private StandardPrerequisitePointResponse toStandardResponse(PrerequisiteStandard standard) {
        return new StandardPrerequisitePointResponse(
            standard.getId(),
            standard.getStandardName(),
            "standard",
            standard.getStandardDescription()
        );
    }

    /**
     * Maps a Routine entity to its point response DTO, processing nested role bridges.
     */
    private RoutinePrerequisitePointResponse toRoutineResponse(
            PrerequisiteRoutine routine,
            List<RoutineUserBridge> routineUserBridges) {
        Map<RoutineUserRole, List<PrerequisiteUserResponse>> usersByRole = new EnumMap<>(RoutineUserRole.class);

        for (RoutineUserBridge bridge : routineUserBridges) {
            usersByRole
                .computeIfAbsent(bridge.getUserRole(), ignored -> new ArrayList<>())
                .add(new PrerequisiteUserResponse(bridge.getUser().getId(), bridge.getUser().getLegalName()));
        }

        usersByRole.values().forEach(users -> users.sort(
            Comparator.comparing(PrerequisiteUserResponse::legalName, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PrerequisiteUserResponse::userId, Comparator.nullsLast(Comparator.naturalOrder()))
        ));

        IntervalRule intervalRule = routine.getIntervalRule();
        PrerequisiteIntervalResponse intervalResponse = intervalRule == null
            ? null
            : new PrerequisiteIntervalResponse(
                intervalRule.getId(),
                intervalRule.getIntervalStart(),
                intervalRule.getIntervalRepeatTime()
            );

        return new RoutinePrerequisitePointResponse(
            routine.getId(),
            routine.getTitle(),
            "routine",
            routine.getDescription(),
            routine.getImmediateCorrectiveAction(),
            buildRepeatText(intervalRule),
            intervalResponse,
            usersByRole.getOrDefault(RoutineUserRole.VERIFIER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.DEVIATION_RECEIVER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.PERFORMER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.DEPUTY, List.of())
        );
    }

    private String buildRepeatText(IntervalRule intervalRule) {
        if (intervalRule == null) {
            return null;
        }

        String startText = REPEAT_TEXT_FORMATTER.format(
            LocalDateTime.ofInstant(Instant.ofEpochSecond(intervalRule.getIntervalStart()), ZoneId.systemDefault())
        );

        return "Starts " + startText + ", repeats every " + humanizeDuration(intervalRule.getIntervalRepeatTime());
    }

    private String humanizeDuration(Long seconds) {
        if (seconds == null) {
            return "unknown interval";
        }
        if (seconds % 604800 == 0) {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week" : " weeks");
        }
        if (seconds % 86400 == 0) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day" : " days");
        }
        if (seconds % 3600 == 0) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour" : " hours");
        }
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
        return seconds + (seconds == 1 ? " second" : " seconds");
    }

    private LinkedHashSet<Long> toOrderedSet(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return userIds.stream()
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int compareCategory(PrerequisiteCategory left, PrerequisiteCategory right) {
        return compareByCreatedAtAndId(left.getCreatedAt(), left.getId(), right.getCreatedAt(), right.getId());
    }

    private int compareStandard(PrerequisiteStandard left, PrerequisiteStandard right) {
        return compareByCreatedAtAndId(left.getCreatedAt(), left.getId(), right.getCreatedAt(), right.getId());
    }

    private int compareRoutine(PrerequisiteRoutine left, PrerequisiteRoutine right) {
        return compareByCreatedAtAndId(left.getCreatedAt(), left.getId(), right.getCreatedAt(), right.getId());
    }

    private int comparePointEnvelope(PointEnvelope left, PointEnvelope right) {
        return compareByCreatedAtAndId(left.createdAt(), left.id(), right.createdAt(), right.id());
    }

    private int compareByCreatedAtAndId(
            LocalDateTime leftCreatedAt,
            Long leftId,
            LocalDateTime rightCreatedAt,
            Long rightId) {
        Comparator<LocalDateTime> localDateComparator = Comparator.nullsLast(Comparator.naturalOrder());
        int createdAtComparison = localDateComparator.compare(leftCreatedAt, rightCreatedAt);
        if (createdAtComparison != 0) {
            return createdAtComparison;
        }
        return Comparator.nullsLast(Comparator.<Long>naturalOrder()).compare(leftId, rightId);
    }

    /**
     * DTO wrapper to allow heterogeneous sorting of Standards and Routines by timestamp.
     */
    private record PointEnvelope(
        LocalDateTime createdAt,
        Long id,
        PrerequisitePointResponse point
    ) {
    }
}