package com.grimni.service;

import java.math.BigDecimal;
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

import com.grimni.domain.Ccp;
import com.grimni.domain.CcpCorrectiveMeasure;
import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.IntervalRule;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.ProductCategory;
import com.grimni.domain.User;
import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.CcpUserBridgeId;
import com.grimni.dto.CcpCorrectiveMeasureResponse;
import com.grimni.dto.CcpIntervalResponse;
import com.grimni.dto.CcpResponse;
import com.grimni.dto.CcpUserResponse;
import com.grimni.dto.CreateCcpCorrectiveMeasureRequest;
import com.grimni.dto.CreateCcpRequest;
import com.grimni.dto.ReplaceCcpAssignmentsRequest;
import com.grimni.dto.UpdateCcpCorrectiveMeasureRequest;
import com.grimni.dto.UpdateCcpRequest;
import com.grimni.repository.CcpCorrectiveMeasureRepository;
import com.grimni.repository.CcpRecordRepository;
import com.grimni.repository.CcpRepository;
import com.grimni.repository.CcpUserBridgeRepository;
import com.grimni.repository.IntervalRuleRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.PrerequisiteRoutineRepository;
import com.grimni.repository.ProductCategoryRepository;
import com.grimni.repository.UserRepository;

@Service
public class CcpService {

    private static final DateTimeFormatter REPEAT_TEXT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CcpRepository ccpRepository;
    private final CcpRecordRepository ccpRecordRepository;
    private final CcpUserBridgeRepository ccpUserBridgeRepository;
    private final CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final IntervalRuleRepository intervalRuleRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final UserRepository userRepository;
    private final PrerequisiteRoutineRepository prerequisiteRoutineRepository;

    public CcpService(
            CcpRepository ccpRepository,
            CcpRecordRepository ccpRecordRepository,
            CcpUserBridgeRepository ccpUserBridgeRepository,
            CcpCorrectiveMeasureRepository ccpCorrectiveMeasureRepository,
            ProductCategoryRepository productCategoryRepository,
            IntervalRuleRepository intervalRuleRepository,
            OrgUserBridgeRepository orgUserBridgeRepository,
            UserRepository userRepository,
            PrerequisiteRoutineRepository prerequisiteRoutineRepository) {
        this.ccpRepository = ccpRepository;
        this.ccpRecordRepository = ccpRecordRepository;
        this.ccpUserBridgeRepository = ccpUserBridgeRepository;
        this.ccpCorrectiveMeasureRepository = ccpCorrectiveMeasureRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.intervalRuleRepository = intervalRuleRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.userRepository = userRepository;
        this.prerequisiteRoutineRepository = prerequisiteRoutineRepository;
    }

    @Transactional(readOnly = true)
    public List<CcpResponse> getAllInfo(Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);

        List<Ccp> ccps = new ArrayList<>(ccpRepository.findByOrganization_Id(orgId));
        ccps.sort(this::compareCcp);

        List<Long> ccpIds = ccps.stream().map(Ccp::getId).toList();
        Map<Long, List<CcpUserBridge>> usersByCcpId = loadUserBridgesByCcpId(ccpIds);
        Map<Long, List<CcpCorrectiveMeasure>> measuresByCcpId = loadCorrectiveMeasuresByCcpId(ccpIds);

        return ccps.stream()
            .map(ccp -> toCcpResponse(
                ccp,
                usersByCcpId.getOrDefault(ccp.getId(), List.of()),
                measuresByCcpId.getOrDefault(ccp.getId(), List.of())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public long getVerificationCount(Long userId, Long orgId, String role) {
        ensureAuthenticatedMember(userId, orgId);
        boolean isManagerOrOwner = "OWNER".equals(role) || "MANAGER".equals(role);
        return ccpRecordRepository.countWaitingVerifications(orgId, userId, isManagerOrOwner);
    }

    @Transactional
    public CcpResponse createCcp(
            CreateCcpRequest request,
            Long authenticatedUserId,
            Long orgId) {
        OrgUserBridge membership = ensureAuthenticatedMember(authenticatedUserId, orgId);
        validateThresholds(request.criticalMin(), request.criticalMax());

        IntervalRule intervalRule = new IntervalRule();
        intervalRule.setIntervalStart(request.intervalStart());
        intervalRule.setIntervalRepeatTime(request.intervalRepeatTime());
        intervalRule = intervalRuleRepository.save(intervalRule);

        Ccp ccp = new Ccp();
        ccp.setCcpName(request.name());
        ccp.setHow(request.how());
        ccp.setEquipment(request.equipment());
        ccp.setInstructionsAndCalibration(request.instructionsAndCalibration());
        ccp.setImmediateCorrectiveAction(request.immediateCorrectiveAction());
        ccp.setCriticalMin(request.criticalMin());
        ccp.setCriticalMax(request.criticalMax());
        ccp.setUnit(request.unit());
        ccp.setMonitoredDescription(request.monitoredDescription());
        ccp.setOrganization(membership.getOrganization());
        ccp.setIntervalRule(intervalRule);
        ccp = ccpRepository.save(ccp);

        List<CcpUserBridge> savedAssignments = replaceAssignmentsInternal(
            ccp,
            request.verifierUserIds(),
            request.deviationReceiverUserIds(),
            request.performerUserIds(),
            request.deputyUserIds()
        );
        List<CcpCorrectiveMeasure> savedMeasures = saveCorrectiveMeasuresInternal(
            ccp,
            request.correctiveMeasures(),
            orgId
        );

        return toCcpResponse(ccp, savedAssignments, savedMeasures);
    }

    @Transactional
    public CcpResponse updateCcp(
            Long ccpId,
            UpdateCcpRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        Ccp ccp = getCcpInOrg(ccpId, orgId);

        BigDecimal nextCriticalMin = request.criticalMin() != null ? request.criticalMin() : ccp.getCriticalMin();
        BigDecimal nextCriticalMax = request.criticalMax() != null ? request.criticalMax() : ccp.getCriticalMax();
        validateThresholds(nextCriticalMin, nextCriticalMax);

        if (request.name() != null) {
            ccp.setCcpName(request.name());
        }
        if (request.how() != null) {
            ccp.setHow(request.how());
        }
        if (request.equipment() != null) {
            ccp.setEquipment(request.equipment());
        }
        if (request.instructionsAndCalibration() != null) {
            ccp.setInstructionsAndCalibration(request.instructionsAndCalibration());
        }
        if (request.immediateCorrectiveAction() != null) {
            ccp.setImmediateCorrectiveAction(request.immediateCorrectiveAction());
        }
        if (request.criticalMin() != null) {
            ccp.setCriticalMin(request.criticalMin());
        }
        if (request.criticalMax() != null) {
            ccp.setCriticalMax(request.criticalMax());
        }
        if (request.unit() != null) {
            ccp.setUnit(request.unit());
        }
        if (request.monitoredDescription() != null) {
            ccp.setMonitoredDescription(request.monitoredDescription());
        }
        if (request.intervalStart() != null || request.intervalRepeatTime() != null) {
            IntervalRule intervalRule = ccp.getIntervalRule();
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
            ccp.setIntervalRule(intervalRule);
        }

        Ccp savedCcp = ccpRepository.save(ccp);
        return toCcpResponse(
            savedCcp,
            loadUserBridges(savedCcp.getId()),
            loadCorrectiveMeasures(savedCcp.getId())
        );
    }

    @Transactional
    public void deleteCcp(Long ccpId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        deleteCcpEntity(getCcpInOrg(ccpId, orgId));
    }

    @Transactional
    public CcpResponse replaceAssignments(
            Long ccpId,
            ReplaceCcpAssignmentsRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        Ccp ccp = getCcpInOrg(ccpId, orgId);

        List<CcpUserBridge> savedAssignments = replaceAssignmentsInternal(
            ccp,
            request.verifierUserIds(),
            request.deviationReceiverUserIds(),
            request.performerUserIds(),
            request.deputyUserIds()
        );

        return toCcpResponse(ccp, savedAssignments, loadCorrectiveMeasures(ccp.getId()));
    }

    @Transactional
    public CcpCorrectiveMeasureResponse createCorrectiveMeasure(
            Long ccpId,
            CreateCcpCorrectiveMeasureRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        Ccp ccp = getCcpInOrg(ccpId, orgId);
        ProductCategory productCategory = getProductCategoryInOrg(request.productCategoryId(), orgId);

        CcpCorrectiveMeasure measure = new CcpCorrectiveMeasure();
        measure.setCcp(ccp);
        measure.setProductCategory(productCategory);
        measure.setMeasureDescription(request.measureDescription());

        return toCorrectiveMeasureResponse(ccpCorrectiveMeasureRepository.save(measure));
    }

    @Transactional
    public CcpCorrectiveMeasureResponse updateCorrectiveMeasure(
            Long measureId,
            UpdateCcpCorrectiveMeasureRequest request,
            Long authenticatedUserId,
            Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        CcpCorrectiveMeasure measure = getCorrectiveMeasureInOrg(measureId, orgId);

        if (request.productCategoryId() != null) {
            measure.setProductCategory(getProductCategoryInOrg(request.productCategoryId(), orgId));
        }
        if (request.measureDescription() != null) {
            measure.setMeasureDescription(request.measureDescription());
        }

        return toCorrectiveMeasureResponse(ccpCorrectiveMeasureRepository.save(measure));
    }

    @Transactional
    public void deleteCorrectiveMeasure(Long measureId, Long authenticatedUserId, Long orgId) {
        ensureAuthenticatedMember(authenticatedUserId, orgId);
        ccpCorrectiveMeasureRepository.delete(getCorrectiveMeasureInOrg(measureId, orgId));
    }

    private OrgUserBridge ensureAuthenticatedMember(Long authenticatedUserId, Long orgId) {
        return orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private Ccp getCcpInOrg(Long ccpId, Long orgId) {
        return ccpRepository.findByIdAndOrganization_Id(ccpId, orgId)
            .orElseThrow(() -> new RuntimeException("Critical control point not found"));
    }

    private ProductCategory getProductCategoryInOrg(Long productCategoryId, Long orgId) {
        return productCategoryRepository.findByIdAndOrganization_Id(productCategoryId, orgId)
            .orElseThrow(() -> new IllegalArgumentException("Product category must belong to the active organization"));
    }

    private CcpCorrectiveMeasure getCorrectiveMeasureInOrg(Long measureId, Long orgId) {
        return ccpCorrectiveMeasureRepository.findByIdAndCcp_Organization_Id(measureId, orgId)
            .orElseThrow(() -> new RuntimeException("CCP corrective measure not found"));
    }

    private List<CcpUserBridge> replaceAssignmentsInternal(
            Ccp ccp,
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

        Map<Long, User> usersById = loadUsersInOrganization(allUserIds, ccp.getOrganization().getId());

        ccpUserBridgeRepository.deleteByCcp_Id(ccp.getId());

        List<CcpUserBridge> newBridges = new ArrayList<>();
        for (Map.Entry<RoutineUserRole, LinkedHashSet<Long>> entry : requestedAssignments.entrySet()) {
            for (Long userId : entry.getValue()) {
                CcpUserBridge bridge = new CcpUserBridge();
                bridge.setId(new CcpUserBridgeId(userId, ccp.getId(), entry.getKey()));
                bridge.setCcp(ccp);
                bridge.setUser(usersById.get(userId));
                bridge.setUserRole(entry.getKey());
                newBridges.add(bridge);
            }
        }

        if (newBridges.isEmpty()) {
            return List.of();
        }

        return ccpUserBridgeRepository.saveAll(newBridges);
    }

    private List<CcpCorrectiveMeasure> saveCorrectiveMeasuresInternal(
            Ccp ccp,
            List<CreateCcpCorrectiveMeasureRequest> requestedMeasures,
            Long orgId) {
        if (requestedMeasures == null || requestedMeasures.isEmpty()) {
            return List.of();
        }

        Set<Long> productCategoryIds = requestedMeasures.stream()
            .map(CreateCcpCorrectiveMeasureRequest::productCategoryId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ProductCategory> productCategoriesById = loadProductCategoriesInOrganization(productCategoryIds, orgId);

        List<CcpCorrectiveMeasure> measures = requestedMeasures.stream()
            .map(request -> {
                CcpCorrectiveMeasure measure = new CcpCorrectiveMeasure();
                measure.setCcp(ccp);
                measure.setProductCategory(productCategoriesById.get(request.productCategoryId()));
                measure.setMeasureDescription(request.measureDescription());
                return measure;
            })
            .toList();

        return ccpCorrectiveMeasureRepository.saveAll(measures);
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

    private Map<Long, ProductCategory> loadProductCategoriesInOrganization(Set<Long> productCategoryIds, Long orgId) {
        if (productCategoryIds.isEmpty()) {
            return Map.of();
        }

        List<ProductCategory> productCategories = productCategoryRepository.findByOrganization_IdAndIdIn(orgId, productCategoryIds);
        if (productCategories.size() != productCategoryIds.size()) {
            throw new IllegalArgumentException("Product category must belong to the active organization");
        }

        return productCategories.stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
    }

    private List<CcpUserBridge> loadUserBridges(Long ccpId) {
        if (ccpId == null) {
            return List.of();
        }

        return ccpUserBridgeRepository.findAllWithUserByCcpIds(List.of(ccpId));
    }

    private Map<Long, List<CcpUserBridge>> loadUserBridgesByCcpId(List<Long> ccpIds) {
        if (ccpIds.isEmpty()) {
            return Map.of();
        }

        return ccpUserBridgeRepository.findAllWithUserByCcpIds(ccpIds).stream()
            .collect(Collectors.groupingBy(bridge -> bridge.getCcp().getId()));
    }

    private List<CcpCorrectiveMeasure> loadCorrectiveMeasures(Long ccpId) {
        if (ccpId == null) {
            return List.of();
        }

        return ccpCorrectiveMeasureRepository.findAllWithProductCategoryByCcpIds(List.of(ccpId));
    }

    private Map<Long, List<CcpCorrectiveMeasure>> loadCorrectiveMeasuresByCcpId(List<Long> ccpIds) {
        if (ccpIds.isEmpty()) {
            return Map.of();
        }

        return ccpCorrectiveMeasureRepository.findAllWithProductCategoryByCcpIds(ccpIds).stream()
            .collect(Collectors.groupingBy(measure -> measure.getCcp().getId()));
    }

    private void deleteCcpEntity(Ccp ccp) {
        Long intervalId = ccp.getIntervalRule() != null ? ccp.getIntervalRule().getId() : null;

        ccpUserBridgeRepository.deleteByCcp_Id(ccp.getId());
        ccpCorrectiveMeasureRepository.deleteByCcp_Id(ccp.getId());
        ccpRepository.delete(ccp);
        ccpRepository.flush();
        cleanupIntervalIfOrphaned(intervalId);
    }

    private void cleanupIntervalIfOrphaned(Long intervalId) {
        if (intervalId == null) {
            return;
        }

        boolean usedByCcp = ccpRepository.existsByIntervalRule_Id(intervalId);
        boolean usedByRoutine = prerequisiteRoutineRepository.existsByIntervalRule_Id(intervalId);

        if (!usedByCcp && !usedByRoutine) {
            intervalRuleRepository.deleteById(intervalId);
        }
    }

    private void validateThresholds(BigDecimal criticalMin, BigDecimal criticalMax) {
        if (criticalMin != null && criticalMax != null && criticalMin.compareTo(criticalMax) > 0) {
            throw new IllegalArgumentException("Critical minimum cannot be greater than critical maximum");
        }
    }

    private CcpResponse toCcpResponse(
            Ccp ccp,
            List<CcpUserBridge> userBridges,
            List<CcpCorrectiveMeasure> correctiveMeasures) {
        Map<RoutineUserRole, List<CcpUserResponse>> usersByRole = new EnumMap<>(RoutineUserRole.class);
        for (CcpUserBridge bridge : userBridges) {
            usersByRole
                .computeIfAbsent(bridge.getUserRole(), ignored -> new ArrayList<>())
                .add(new CcpUserResponse(bridge.getUser().getId(), bridge.getUser().getLegalName()));
        }

        usersByRole.values().forEach(users -> users.sort(
            Comparator.comparing(CcpUserResponse::legalName, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CcpUserResponse::userId, Comparator.nullsLast(Comparator.naturalOrder()))
        ));

        List<CcpCorrectiveMeasureResponse> correctiveMeasureResponses = correctiveMeasures.stream()
            .sorted(this::compareCorrectiveMeasure)
            .map(this::toCorrectiveMeasureResponse)
            .toList();

        IntervalRule intervalRule = ccp.getIntervalRule();
        CcpIntervalResponse intervalResponse = intervalRule == null
            ? null
            : new CcpIntervalResponse(
                intervalRule.getId(),
                intervalRule.getIntervalStart(),
                intervalRule.getIntervalRepeatTime()
            );

        return new CcpResponse(
            ccp.getId(),
            ccp.getCcpName(),
            ccp.getHow(),
            ccp.getEquipment(),
            ccp.getInstructionsAndCalibration(),
            ccp.getImmediateCorrectiveAction(),
            ccp.getCriticalMin(),
            ccp.getCriticalMax(),
            ccp.getUnit(),
            ccp.getMonitoredDescription(),
            intervalResponse,
            buildRepeatText(intervalRule),
            usersByRole.getOrDefault(RoutineUserRole.VERIFIER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.DEVIATION_RECEIVER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.PERFORMER, List.of()),
            usersByRole.getOrDefault(RoutineUserRole.DEPUTY, List.of()),
            correctiveMeasureResponses
        );
    }

    private CcpCorrectiveMeasureResponse toCorrectiveMeasureResponse(CcpCorrectiveMeasure measure) {
        ProductCategory productCategory = measure.getProductCategory();
        return new CcpCorrectiveMeasureResponse(
            measure.getId(),
            productCategory != null ? productCategory.getId() : null,
            productCategory != null ? productCategory.getProductDescription() : null,
            measure.getMeasureDescription()
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

    private int compareCcp(Ccp left, Ccp right) {
        return compareByCreatedAtAndId(left.getCreatedAt(), left.getId(), right.getCreatedAt(), right.getId());
    }

    private int compareCorrectiveMeasure(CcpCorrectiveMeasure left, CcpCorrectiveMeasure right) {
        return compareByCreatedAtAndId(left.getCreatedAt(), left.getId(), right.getCreatedAt(), right.getId());
    }

    private int compareByCreatedAtAndId(
            LocalDateTime leftCreatedAt,
            Long leftId,
            LocalDateTime rightCreatedAt,
            Long rightId) {
        Comparator<LocalDateTime> dateComparator = Comparator.nullsLast(Comparator.naturalOrder());
        int createdAtComparison = dateComparator.compare(leftCreatedAt, rightCreatedAt);
        if (createdAtComparison != 0) {
            return createdAtComparison;
        }
        return Comparator.nullsLast(Comparator.<Long>naturalOrder()).compare(leftId, rightId);
    }
}
