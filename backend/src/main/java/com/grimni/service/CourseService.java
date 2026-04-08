package com.grimni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grimni.domain.Course;
import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.ids.CourseUserProgressId;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.CourseResponsibleUserRepository;
import com.grimni.repository.CourseUserProgressRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final CourseUserProgressRepository courseUserProgressRepository;
    private final CourseResponsibleUserRepository courseResponsibleUserRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository,
                         OrganizationRepository organizationRepository,
                         OrgUserBridgeRepository orgUserBridgeRepository,
                         CourseUserProgressRepository courseUserProgressRepository,
                         CourseResponsibleUserRepository courseResponsibleUserRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.organizationRepository = organizationRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.courseUserProgressRepository = courseUserProgressRepository;
        this.courseResponsibleUserRepository = courseResponsibleUserRepository;
        this.userRepository = userRepository;
    }

    private void validateUserBelongsToOrg(Long orgId, Long userId) {
        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("User {} does not belong to organization {}", userId, orgId);
                    return new EntityNotFoundException("Organization not found");
                });
    }

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

    public List<Course> getCoursesByOrg(Long orgId, Long userId) {
        logger.info("Fetching courses for organization {} by user {}", orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        List<Course> courses = courseRepository.findByOrganizationId(orgId);
        logger.info("Found {} courses for organization {}", courses.size(), orgId);
        return courses;
    }

    public Course getCourseById(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching course {} in organization {} by user {}", courseId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Course course = findCourseAndValidateOrg(courseId, orgId);
        logger.info("Course {} found in organization {}", courseId, orgId);
        return course;
    }

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

    public void deleteCourse(Long courseId, Long orgId, Long userId) {
        logger.info("Deleting course {} in organization {} by user {}", courseId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Course course = findCourseAndValidateOrg(courseId, orgId);

        courseRepository.delete(course);
        logger.info("Course {} deleted from organization {}", courseId, orgId);
    }

    // Course User Progress
    public List<CourseUserProgress> getProgressByCourse(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching all assigned users for course {} in organization {}", courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        List<CourseUserProgress> progress = courseUserProgressRepository.findByCourseId(courseId);
        logger.info("Found {} assigned users for course {}", progress.size(), courseId);
        return progress;
    }

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

    // Course Responsible Users (verifiers)
    public List<CourseResponsibleUser> getResponsibleUsers(Long courseId, Long orgId, Long userId) {
        logger.info("Fetching responsible users for course {} in organization {}", courseId, orgId);

        validateUserBelongsToOrg(orgId, userId);
        findCourseAndValidateOrg(courseId, orgId);

        List<CourseResponsibleUser> responsible = courseResponsibleUserRepository.findByCourseId(courseId);
        logger.info("Found {} responsible users for course {}", responsible.size(), courseId);
        return responsible;
    }

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
}
