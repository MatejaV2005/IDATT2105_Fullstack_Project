package com.grimni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grimni.domain.Course;
import com.grimni.domain.CourseLink;
import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.ids.CourseUserProgressId;
import com.grimni.dto.CourseOverviewResponse;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.repository.CourseLinkRepository;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.CourseResponsibleUserRepository;
import com.grimni.repository.CourseUserProgressRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing organizational training courses, user enrollment, and progress tracking.
 * <p>
 * This service facilitates the full educational lifecycle within an organization, including:
 * <ul>
 * <li>Course administration (CRUD operations).</li>
 * <li>User enrollment and completion tracking.</li>
 * <li>Responsibility mapping for verifiers and administrative oversight.</li>
 * <li>Comprehensive reporting through organizational course overviews.</li>
 * </ul>
 * All operations are governed by organizational multi-tenancy rules to ensure data isolation.
 */
@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final CourseUserProgressRepository courseUserProgressRepository;
    private final CourseResponsibleUserRepository courseResponsibleUserRepository;
    private final UserRepository userRepository;
    private final CourseLinkRepository courseLinkRepository;

    public CourseService(CourseRepository courseRepository,
                         OrganizationRepository organizationRepository,
                         OrgUserBridgeRepository orgUserBridgeRepository,
                         CourseUserProgressRepository courseUserProgressRepository,
                         CourseResponsibleUserRepository courseResponsibleUserRepository,
                         UserRepository userRepository,
                         CourseLinkRepository courseLinkRepository) {
        this.courseRepository = courseRepository;
        this.organizationRepository = organizationRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.courseUserProgressRepository = courseUserProgressRepository;
        this.courseResponsibleUserRepository = courseResponsibleUserRepository;
        this.userRepository = userRepository;
        this.courseLinkRepository = courseLinkRepository;
    }

    /**
     * Creates a new course together with its associated reference links in a single transaction.
     *
     * @param title       the course title.
     * @param description the course description.
     * @param links       optional list of reference URLs to attach to the course.
     * @param orgId       the organization scope.
     * @param userId      the ID of the user performing the creation.
     * @return the persisted {@link Course} entity.
     */
    @Transactional
    public Course createCourseWithLinks(String title, String description, List<String> links, Long orgId, Long userId) {
        validateUserBelongsToOrg(orgId, userId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Course course = new Course();
        course.setTitle(title);
        course.setCourseDescription(description);
        course.setOrganization(org);
        course = courseRepository.save(course);

        if (links != null) {
            for (String link : links) {
                if (link == null || link.isBlank()) {
                    continue;
                }
                CourseLink courseLink = new CourseLink();
                courseLink.setCourse(course);
                courseLink.setLink(link.trim());
                courseLinkRepository.save(courseLink);
            }
        }

        return course;
    }

    /**
     * Validates that a user holds a valid membership within a specific organization.
     *
     * @param orgId  the unique identifier of the organization.
     * @param userId the unique identifier of the user.
     * @throws EntityNotFoundException if no membership bridge exists between the user and organization.
     */
    private void validateUserBelongsToOrg(Long orgId, Long userId) {
        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("User {} does not belong to organization {}", userId, orgId);
                    return new EntityNotFoundException("Organization not found");
                });
    }

    /**
     * Retrieves a course and ensures it is associated with the provided organization ID.
     *
     * @param courseId the unique identifier of the course.
     * @param orgId    the organization ID to validate against.
     * @return the {@link Course} entity.
     * @throws EntityNotFoundException if the course is not found or belongs to a different organization.
     */
    private Course findCourseAndValidateOrg(Long courseId, Long orgId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    logger.warn("Course {} not found", courseId);
                    return new EntityNotFoundException("Course not found");
                });
        if (!course.getOrganization().getId().equals(orgId)) {
            logger.warn("Course {} does not belong to organization {}", courseId, orgId);
            throw new EntityNotFoundException("Course not found");
        }
        return course;
    }

    /**
     * Registers a new training course within the organization.
     *
     * @param request the course details (title and description).
     * @param orgId   the organization ID scope.
     * @param userId  the ID of the user performing the creation.
     * @return the persisted {@link Course} entity.
     * @throws EntityNotFoundException if the organization is not found.
     */
    public Course createCourse(CreateCourseRequest request, Long orgId, Long userId) {
        logger.info("Creating course '{}' in organization {} by user {}", request.title(), orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    logger.warn("Create course failed: organization {} not found", orgId);
                    return new EntityNotFoundException("Organization not found");
                });

        Course course = new Course();
        course.setTitle(request.title());
        course.setCourseDescription(request.courseDescription());
        course.setOrganization(org);
        course = courseRepository.save(course);

        logger.info("Course '{}' (id={}) created in organization {}", course.getTitle(), course.getId(), orgId);
        return course;
    }

    /**
     * Lists all courses available within a specific organization.
     *
     * @param orgId  the organization ID.
     * @param userId the ID of the requesting user.
     * @return a list of {@link Course} entities.
     */
    public List<Course> getCoursesByOrg(Long orgId, Long userId) {
        logger.info("Fetching courses for organization {} by user {}", orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        List<Course> courses = courseRepository.findByOrganizationId(orgId);
        logger.info("Found {} courses for organization {}", courses.size(), orgId);
        return courses;
    }

    /**
     * Fetches detailed information for a single course.
     *
     * @param courseId the ID of the course.
     * @param orgId    the organization ID for scope validation.
     * @param userId   the ID of the requesting user.
     * @return the validated {@link Course} entity.
     */
    public Course getCourseById(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching course {} in organization {} by user {}", courseId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Course course = findCourseAndValidateOrg(courseId, orgId);
        logger.info("Course {} found in organization {}", courseId, orgId);
        return course;
    }

    /**
     * Updates an existing course's metadata.
     *
     * @param courseId the ID of the course to update.
     * @param request  the updated fields (title, description).
     * @param orgId    the organization ID for scope validation.
     * @param userId   the ID of the user performing the update.
     * @return the updated {@link Course} entity.
     */
    public Course updateCourse(Long courseId, UpdateCourseRequest request, Long orgId, Long userId) {
        logger.info("Updating course {} in organization {} by user {}", courseId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Course course = findCourseAndValidateOrg(courseId, orgId);

        if (request.title() != null) course.setTitle(request.title());
        if (request.courseDescription() != null) course.setCourseDescription(request.courseDescription());

        course = courseRepository.save(course);
        logger.info("Course {} updated in organization {}", courseId, orgId);
        return course;
    }

    /**
     * Deletes a course from the organization.
     *
     * @param courseId the ID of the course to remove.
     * @param orgId    the organization ID for scope validation.
     * @param userId   the ID of the user performing the deletion.
     */
    public void deleteCourse(Long courseId, Long orgId, Long userId) {
        logger.info("Deleting course {} in organization {} by user {}", courseId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Course course = findCourseAndValidateOrg(courseId, orgId);

        courseRepository.delete(course);
        logger.info("Course {} deleted from organization {}", courseId, orgId);
    }

    /**
     * Retrieves all user progress records associated with a specific course.
     *
     * @param courseId the unique identifier of the course.
     * @param orgId    the organization ID.
     * @param userId   the ID of the requesting user.
     * @return a list of {@link CourseUserProgress} entities.
     */
    public List<CourseUserProgress> getProgressByCourse(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching all assigned users for course {} in organization {}", courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        List<CourseUserProgress> progress = courseUserProgressRepository.findByCourseId(courseId);
        logger.info("Found {} assigned users for course {}", progress.size(), courseId);
        return progress;
    }

    /**
     * Fetches the completion status and progress details for a specific user on a course.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user whose progress is being queried.
     * @param orgId        the organization ID.
     * @param userId       the ID of the requesting user.
     * @return the {@link CourseUserProgress} record.
     * @throws EntityNotFoundException if the user has no progress record for the course.
     */
    public CourseUserProgress getProgressForUser(Long courseId, Long targetUserId, Long orgId, Long userId) {
        logger.info("Fetching progress for user {} on course {} in organization {}", targetUserId, courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        return courseUserProgressRepository.findByCourseIdAndUserId(courseId, targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Progress not found for user {} on course {}", targetUserId, courseId);
                    return new EntityNotFoundException("Progress not found");
                });
    }

    /**
     * Assigns a user to a course and initializes their progress record.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user to enroll.
     * @param orgId        the organization ID scope.
     * @param userId       the ID of the user performing the assignment.
     * @return the newly created {@link CourseUserProgress} entity.
     * @throws IllegalArgumentException if the user is already assigned to the course.
     * @throws EntityNotFoundException if the target user or course is not found.
     */
    public CourseUserProgress assignUserToCourse(Long courseId, Long targetUserId, Long orgId, Long userId) {
        logger.info("Assigning user {} to course {} in organization {}", targetUserId, courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        validateUserBelongsToOrg(orgId, targetUserId);
        Course course = findCourseAndValidateOrg(courseId, orgId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Assign failed: user {} not found", targetUserId);
                    return new EntityNotFoundException("User not found");
                });

        if (courseUserProgressRepository.findByCourseIdAndUserId(courseId, targetUserId).isPresent()) {
            throw new IllegalArgumentException("User is already assigned to this course");
        }

        CourseUserProgress progress = new CourseUserProgress();
        progress.setId(new CourseUserProgressId(courseId, targetUserId));
        progress.setCourse(course);
        progress.setUser(targetUser);
        progress.setIsCompleted(false);
        progress = courseUserProgressRepository.save(progress);

        logger.info("User {} assigned to course {} in organization {}", targetUserId, courseId, orgId);
        return progress;
    }

    /**
     * Updates the completion status of a user's course enrollment.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user.
     * @param isCompleted  the new completion status.
     * @param orgId        the organization ID.
     * @param userId       the ID of the user performing the update.
     * @return the updated {@link CourseUserProgress} entity.
     * @throws EntityNotFoundException if no progress record exists for the given user and course.
     */
    public CourseUserProgress updateProgress(Long courseId, Long targetUserId, Boolean isCompleted, Long orgId, Long userId) {
        logger.info("Updating progress for user {} on course {} to completed={}", targetUserId, courseId, isCompleted);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        CourseUserProgress progress = courseUserProgressRepository.findByCourseIdAndUserId(courseId, targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Update progress failed: no record for user {} on course {}", targetUserId, courseId);
                    return new EntityNotFoundException("Progress not found");
                });

        progress.setIsCompleted(isCompleted);
        progress = courseUserProgressRepository.save(progress);

        logger.info("Progress updated for user {} on course {}: completed={}", targetUserId, courseId, isCompleted);
        return progress;
    }

    /**
     * Unenrolls a user from a course, removing their progress record.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user to remove.
     * @param orgId        the organization ID.
     * @param userId       the ID of the requesting user.
     * @throws EntityNotFoundException if the progress record is missing.
     */
    public void removeUserFromCourse(Long courseId, Long targetUserId, Long orgId, Long userId) {
        logger.info("Removing user {} from course {} in organization {}", targetUserId, courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        CourseUserProgress progress = courseUserProgressRepository.findByCourseIdAndUserId(courseId, targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Remove failed: no progress record for user {} on course {}", targetUserId, courseId);
                    return new EntityNotFoundException("Progress not found");
                });

        courseUserProgressRepository.delete(progress);
        logger.info("User {} removed from course {} in organization {}", targetUserId, courseId, orgId);
    }

    /**
     * Retrieves the list of users designated as responsible (verifiers) for a specific course.
     *
     * @param courseId the ID of the course.
     * @param orgId    the organization ID.
     * @param userId   the ID of the requesting user.
     * @return a list of {@link CourseResponsibleUser} entities.
     */
    public List<CourseResponsibleUser> getResponsibleUsers(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching responsible users for course {} in organization {}", courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        List<CourseResponsibleUser> responsible = courseResponsibleUserRepository.findByCourseId(courseId);
        logger.info("Found {} responsible users for course {}", responsible.size(), courseId);
        return responsible;
    }

    /**
     * Assigns a user as a responsible party (verifier/manager) for a specific course.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user to designate.
     * @param orgId        the organization ID.
     * @param userId       the ID of the user performing the assignment.
     * @return the persisted {@link CourseResponsibleUser} entity.
     * @throws IllegalArgumentException if the user is already assigned as responsible for the course.
     * @throws EntityNotFoundException if the target user or course is missing.
     */
    public CourseResponsibleUser assignResponsibleUser(Long courseId, Long targetUserId, Long orgId, Long userId) {
        logger.info("Assigning user {} as responsible for course {} in organization {}", targetUserId, courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        validateUserBelongsToOrg(orgId, targetUserId);
        Course course = findCourseAndValidateOrg(courseId, orgId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Assign responsible failed: user {} not found", targetUserId);
                    return new EntityNotFoundException("User not found");
                });

        if (courseResponsibleUserRepository.findByCourseIdAndUserId(courseId, targetUserId).isPresent()) {
            throw new IllegalArgumentException("User is already a responsible user for this course");
        }

        CourseResponsibleUser responsible = new CourseResponsibleUser();
        responsible.setId(new CourseUserProgressId(courseId, targetUserId));
        responsible.setCourse(course);
        responsible.setUser(targetUser);
        responsible = courseResponsibleUserRepository.save(responsible);

        logger.info("User {} assigned as responsible for course {} in organization {}", targetUserId, courseId, orgId);
        return responsible;
    }

    /**
     * Revokes the responsibility status of a user for a specific course.
     *
     * @param courseId     the ID of the course.
     * @param targetUserId the ID of the user to remove.
     * @param orgId        the organization ID.
     * @param userId       the ID of the requesting user.
     * @throws EntityNotFoundException if the user was not designated as responsible for the course.
     */
    public void removeResponsibleUser(Long courseId, Long targetUserId, Long orgId, Long userId) {
        logger.info("Removing responsible user {} from course {} in organization {}", targetUserId, courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        CourseResponsibleUser responsible = courseResponsibleUserRepository.findByCourseIdAndUserId(courseId, targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Remove responsible failed: user {} not responsible for course {}", targetUserId, courseId);
                    return new EntityNotFoundException("Responsible user not found");
                });

        courseResponsibleUserRepository.delete(responsible);
        logger.info("Responsible user {} removed from course {} in organization {}", targetUserId, courseId, orgId);
    }

    /**
     * Compiles a comprehensive overview of all courses and user progress for an entire organization.
     *
     * @param orgId  the organization ID.
     * @param userId the ID of the user requesting the overview.
     * @return a {@link CourseOverviewResponse} containing detailed statistics and individual progress mappings.
     */
    public CourseOverviewResponse getCourseOverview(Long orgId, Long userId) {
        logger.info("Fetching course overview for organization {}", orgId);

        validateUserBelongsToOrg(orgId, userId);

        List<Course> courses = courseRepository.findByOrganizationId(orgId);

        List<CourseOverviewResponse.CourseDetailResponse> allCourses = courses.stream()
                .map(course -> {
                    List<String> responsible = courseResponsibleUserRepository.findByCourseId(course.getId())
                            .stream()
                            .map(r -> r.getUser().getLegalName())
                            .toList();
                    return new CourseOverviewResponse.CourseDetailResponse(
                            course.getId(),
                            course.getTitle(),
                            course.getCourseDescription(),
                            responsible
                    );
                }).toList();

        List<CourseUserProgress> allProgress = courseUserProgressRepository.findByCourseIdIn(
                courses.stream().map(Course::getId).toList()
        );

        List<CourseOverviewResponse.UserProgressOverview> userProgress = allProgress.stream()
                .collect(java.util.stream.Collectors.groupingBy(p -> p.getUser().getId()))
                .entrySet().stream()
                .map(entry -> {
                    CourseUserProgress first = entry.getValue().get(0);
                    List<CourseOverviewResponse.UserCourseStatus> statuses = entry.getValue().stream()
                            .map(p -> new CourseOverviewResponse.UserCourseStatus(
                                    p.getCourse().getId(),
                                    p.getCourse().getTitle(),
                                    p.getIsCompleted()
                            )).toList();
                    return new CourseOverviewResponse.UserProgressOverview(
                            first.getUser().getId(),
                            first.getUser().getLegalName(),
                            statuses
                    );
                }).toList();

        logger.info("Course overview assembled: {} courses, {} users", allCourses.size(), userProgress.size());
        return new CourseOverviewResponse(allCourses, userProgress);
    }

}