package com.grimni.backend.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.Course;
import com.grimni.domain.CourseLink;
import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.FileCourseBridge;
import com.grimni.domain.FileObject;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CourseOverviewResponse;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.repository.CourseLinkRepository;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.CourseResponsibleUserRepository;
import com.grimni.repository.CourseUserProgressRepository;
import com.grimni.repository.FileCourseBridgeRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.CourseService;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrgUserBridgeRepository orgUserBridgeRepository;
    @Mock private CourseUserProgressRepository courseUserProgressRepository;
    @Mock private CourseResponsibleUserRepository courseResponsibleUserRepository;
    @Mock private CourseLinkRepository courseLinkRepository;
    @Mock private FileCourseBridgeRepository fileCourseBridgeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private Organization testOrg;
    private User testUser;
    private User targetUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        ReflectionTestUtils.setField(testOrg, "id", 10L);
        testOrg.setOrgName("Test Org");

        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setLegalName("alice");

        targetUser = new User();
        ReflectionTestUtils.setField(targetUser, "id", 2L);
        targetUser.setLegalName("bob");

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Safety Course");
        testCourse.setCourseDescription("Learn safety");
        testCourse.setOrganization(testOrg);
    }

    private void stubOrgMembership(Long orgId, Long userId) {
        when(orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId))
                .thenReturn(Optional.of(new OrgUserBridge()));
    }

    // =========================================================================
    // CRUD
    // =========================================================================

    @Nested
    @DisplayName("createCourse")
    class CreateCourseTests {

        @Test
        @DisplayName("creates course and returns it")
        void createCourse_success() {
            CreateCourseRequest request = new CreateCourseRequest("Safety Course", "Learn safety");
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(testOrg));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
                Course c = inv.getArgument(0);
                c.setId(100L);
                return c;
            });

            Course result = courseService.createCourse(request, 10L, 1L);

            assertEquals("Safety Course", result.getTitle());
            assertEquals("Learn safety", result.getCourseDescription());
            assertEquals(testOrg, result.getOrganization());
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("throws when user not in org")
        void createCourse_userNotInOrg_throws() {
            CreateCourseRequest request = new CreateCourseRequest("Title", "Desc");
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.createCourse(request, 10L, 99L));

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when organization not found")
        void createCourse_orgNotFound_throws() {
            CreateCourseRequest request = new CreateCourseRequest("Title", "Desc");
            stubOrgMembership(10L, 1L);
            when(organizationRepository.findById(10L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.createCourse(request, 10L, 1L));

            assertEquals("Organization not found", ex.getMessage());
            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCoursesByOrg")
    class GetCoursesByOrgTests {

        @Test
        @DisplayName("returns courses for org")
        void getCoursesByOrg_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findByOrganizationId(10L)).thenReturn(List.of(testCourse));

            List<Course> result = courseService.getCoursesByOrg(10L, 1L);

            assertEquals(1, result.size());
            assertEquals("Safety Course", result.get(0).getTitle());
        }

        @Test
        @DisplayName("returns empty list when no courses")
        void getCoursesByOrg_empty() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findByOrganizationId(10L)).thenReturn(List.of());

            List<Course> result = courseService.getCoursesByOrg(10L, 1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("throws when user not in org")
        void getCoursesByOrg_notMember_throws() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.getCoursesByOrg(10L, 99L));

            verify(courseRepository, never()).findByOrganizationId(any());
        }
    }

    @Nested
    @DisplayName("getCourseById")
    class GetCourseByIdTests {

        @Test
        @DisplayName("returns course when found and belongs to org")
        void getCourseById_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            Course result = courseService.getCourseById(100L, 10L, 1L);

            assertEquals("Safety Course", result.getTitle());
        }

        @Test
        @DisplayName("throws when course not found")
        void getCourseById_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.getCourseById(999L, 10L, 1L));

            assertEquals("Course not found", ex.getMessage());
        }

        @Test
        @DisplayName("throws when course belongs to different org")
        void getCourseById_wrongOrg_throws() {
            stubOrgMembership(20L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.getCourseById(100L, 20L, 1L));

            assertEquals("Course not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourseTests {

        @Test
        @DisplayName("updates all provided fields")
        void updateCourse_allFields() {
            UpdateCourseRequest request = new UpdateCourseRequest("New Title", "New Desc");
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            Course result = courseService.updateCourse(100L, request, 10L, 1L);

            assertEquals("New Title", result.getTitle());
            assertEquals("New Desc", result.getCourseDescription());
        }

        @Test
        @DisplayName("partial update — only title")
        void updateCourse_partialUpdate() {
            UpdateCourseRequest request = new UpdateCourseRequest("New Title", null);
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            Course result = courseService.updateCourse(100L, request, 10L, 1L);

            assertEquals("New Title", result.getTitle());
            assertEquals("Learn safety", result.getCourseDescription());
        }

        @Test
        @DisplayName("throws when course not found")
        void updateCourse_notFound_throws() {
            UpdateCourseRequest request = new UpdateCourseRequest("Title", null);
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.updateCourse(999L, request, 10L, 1L));

            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourseTests {

        @Test
        @DisplayName("deletes course successfully")
        void deleteCourse_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            courseService.deleteCourse(100L, 10L, 1L);

            verify(courseRepository).delete(testCourse);
        }

        @Test
        @DisplayName("throws when course not found")
        void deleteCourse_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.deleteCourse(999L, 10L, 1L));

            verify(courseRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws when course belongs to different org")
        void deleteCourse_wrongOrg_throws() {
            stubOrgMembership(20L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.deleteCourse(100L, 20L, 1L));

            verify(courseRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // Course User Progress
    // =========================================================================

    @Nested
    @DisplayName("assignUserToCourse")
    class AssignUserToCourseTests {

        @Test
        @DisplayName("assigns user and returns progress")
        void assignUserToCourse_success() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());
            when(courseUserProgressRepository.save(any(CourseUserProgress.class))).thenAnswer(inv -> inv.getArgument(0));

            CourseUserProgress result = courseService.assignUserToCourse(100L, 2L, 10L, 1L);

            assertNotNull(result);
            assertEquals(testCourse, result.getCourse());
            assertEquals(targetUser, result.getUser());
            assertFalse(result.getIsCompleted());

            ArgumentCaptor<CourseUserProgress> captor = ArgumentCaptor.forClass(CourseUserProgress.class);
            verify(courseUserProgressRepository).save(captor.capture());
            CourseUserProgress saved = captor.getValue();
            assertEquals(100L, saved.getId().hashCode() != 0 ? saved.getCourse().getId() : null);
        }

        @Test
        @DisplayName("throws when user already assigned")
        void assignUserToCourse_alreadyAssigned_throws() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L))
                    .thenReturn(Optional.of(new CourseUserProgress()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> courseService.assignUserToCourse(100L, 2L, 10L, 1L));

            assertEquals("User is already assigned to this course", ex.getMessage());
            verify(courseUserProgressRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target user not found")
        void assignUserToCourse_targetNotFound_throws() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.assignUserToCourse(100L, 2L, 10L, 1L));

            verify(courseUserProgressRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target user not in org")
        void assignUserToCourse_targetNotInOrg_throws() {
            stubOrgMembership(10L, 1L);
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 2L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.assignUserToCourse(100L, 2L, 10L, 1L));

            verify(courseUserProgressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getProgressByCourse")
    class GetProgressByCourseTests {

        @Test
        @DisplayName("returns progress list for course")
        void getProgressByCourse_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(true);
            when(courseUserProgressRepository.findByCourseId(100L)).thenReturn(List.of(p));

            List<CourseUserProgress> result = courseService.getProgressByCourse(100L, 10L, 1L);

            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsCompleted());
        }
    }

    @Nested
    @DisplayName("getProgressForUser")
    class GetProgressForUserTests {

        @Test
        @DisplayName("returns progress for specific user")
        void getProgressForUser_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(false);
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.of(p));

            CourseUserProgress result = courseService.getProgressForUser(100L, 2L, 10L, 1L);

            assertFalse(result.getIsCompleted());
        }

        @Test
        @DisplayName("throws when progress not found")
        void getProgressForUser_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.getProgressForUser(100L, 2L, 10L, 1L));

            assertEquals("Progress not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("updateProgress")
    class UpdateProgressTests {

        @Test
        @DisplayName("updates isCompleted to true")
        void updateProgress_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(false);
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.of(p));
            when(courseUserProgressRepository.save(any(CourseUserProgress.class))).thenAnswer(inv -> inv.getArgument(0));

            CourseUserProgress result = courseService.updateProgress(100L, 2L, true, 10L, 1L);

            assertTrue(result.getIsCompleted());
            verify(courseUserProgressRepository).save(p);
        }

        @Test
        @DisplayName("throws when progress record not found")
        void updateProgress_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.updateProgress(100L, 2L, true, 10L, 1L));

            verify(courseUserProgressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeUserFromCourse")
    class RemoveUserFromCourseTests {

        @Test
        @DisplayName("removes user progress successfully")
        void removeUserFromCourse_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseUserProgress p = new CourseUserProgress();
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.of(p));

            courseService.removeUserFromCourse(100L, 2L, 10L, 1L);

            verify(courseUserProgressRepository).delete(p);
        }

        @Test
        @DisplayName("throws when no progress record exists")
        void removeUserFromCourse_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseUserProgressRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.removeUserFromCourse(100L, 2L, 10L, 1L));

            verify(courseUserProgressRepository, never()).delete(any(CourseUserProgress.class));
        }
    }

    // =========================================================================
    // Course Responsible Users
    // =========================================================================

    @Nested
    @DisplayName("getResponsibleUsers")
    class GetResponsibleUsersTests {

        @Test
        @DisplayName("returns responsible users for course")
        void getResponsibleUsers_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseResponsibleUser r = new CourseResponsibleUser();
            r.setCourse(testCourse);
            r.setUser(targetUser);
            when(courseResponsibleUserRepository.findByCourseId(100L)).thenReturn(List.of(r));

            List<CourseResponsibleUser> result = courseService.getResponsibleUsers(100L, 10L, 1L);

            assertEquals(1, result.size());
            assertEquals(targetUser, result.get(0).getUser());
        }
    }

    @Nested
    @DisplayName("assignResponsibleUser")
    class AssignResponsibleUserTests {

        @Test
        @DisplayName("assigns responsible user successfully")
        void assignResponsibleUser_success() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(courseResponsibleUserRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());
            when(courseResponsibleUserRepository.save(any(CourseResponsibleUser.class))).thenAnswer(inv -> inv.getArgument(0));

            CourseResponsibleUser result = courseService.assignResponsibleUser(100L, 2L, 10L, 1L);

            assertEquals(testCourse, result.getCourse());
            assertEquals(targetUser, result.getUser());
            verify(courseResponsibleUserRepository).save(any(CourseResponsibleUser.class));
        }

        @Test
        @DisplayName("throws when user already responsible")
        void assignResponsibleUser_alreadyAssigned_throws() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
            when(courseResponsibleUserRepository.findByCourseIdAndUserId(100L, 2L))
                    .thenReturn(Optional.of(new CourseResponsibleUser()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> courseService.assignResponsibleUser(100L, 2L, 10L, 1L));

            assertEquals("User is already a responsible user for this course", ex.getMessage());
            verify(courseResponsibleUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target user not found")
        void assignResponsibleUser_targetNotFound_throws() {
            stubOrgMembership(10L, 1L);
            stubOrgMembership(10L, 2L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.assignResponsibleUser(100L, 2L, 10L, 1L));

            verify(courseResponsibleUserRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeResponsibleUser")
    class RemoveResponsibleUserTests {

        @Test
        @DisplayName("removes responsible user successfully")
        void removeResponsibleUser_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseResponsibleUser r = new CourseResponsibleUser();
            when(courseResponsibleUserRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.of(r));

            courseService.removeResponsibleUser(100L, 2L, 10L, 1L);

            verify(courseResponsibleUserRepository).delete(r);
        }

        @Test
        @DisplayName("throws when responsible user not found")
        void removeResponsibleUser_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseResponsibleUserRepository.findByCourseIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.removeResponsibleUser(100L, 2L, 10L, 1L));

            assertEquals("Responsible user not found", ex.getMessage());
            verify(courseResponsibleUserRepository, never()).delete(any(CourseResponsibleUser.class));
        }
    }

    // =========================================================================
    // Course Overview
    // =========================================================================

    @Nested
    @DisplayName("getCourseOverview")
    class GetCourseOverviewTests {

        @Test
        @DisplayName("returns overview with courses, responsible users, and user progress")
        void getCourseOverview_success() {
            stubOrgMembership(10L, 1L);

            Course course2 = new Course();
            course2.setId(200L);
            course2.setTitle("Drink Course");
            course2.setCourseDescription("Learn drinks");
            course2.setOrganization(testOrg);

            when(courseRepository.findByOrganizationId(10L)).thenReturn(List.of(testCourse, course2));

            CourseResponsibleUser r1 = new CourseResponsibleUser();
            r1.setCourse(testCourse);
            r1.setUser(testUser);
            when(courseResponsibleUserRepository.findByCourseId(100L)).thenReturn(List.of(r1));

            CourseResponsibleUser r2 = new CourseResponsibleUser();
            r2.setCourse(course2);
            r2.setUser(targetUser);
            when(courseResponsibleUserRepository.findByCourseId(200L)).thenReturn(List.of(r2));

            CourseUserProgress p1 = new CourseUserProgress();
            p1.setCourse(testCourse);
            p1.setUser(targetUser);
            p1.setIsCompleted(true);

            CourseUserProgress p2 = new CourseUserProgress();
            p2.setCourse(course2);
            p2.setUser(targetUser);
            p2.setIsCompleted(false);

            when(courseUserProgressRepository.findByCourseIdIn(List.of(100L, 200L)))
                    .thenReturn(List.of(p1, p2));

            CourseLink link = new CourseLink();
            ReflectionTestUtils.setField(link, "id", 501L);
            link.setCourse(testCourse);
            link.setLink("https://example.com/safety");
            when(courseLinkRepository.findByCourseId(100L)).thenReturn(List.of(link));
            when(courseLinkRepository.findByCourseId(200L)).thenReturn(List.of());

            FileObject fileObject = new FileObject();
            fileObject.setId(601L);
            fileObject.setFileName("safety.pdf");
            FileCourseBridge fileBridge = new FileCourseBridge();
            fileBridge.setCourse(testCourse);
            fileBridge.setFile(fileObject);
            when(fileCourseBridgeRepository.findByCourseId(100L)).thenReturn(List.of(fileBridge));
            when(fileCourseBridgeRepository.findByCourseId(200L)).thenReturn(List.of());

            CourseOverviewResponse result = courseService.getCourseOverview(10L, 1L);

            // allCourses
            assertEquals(2, result.allCourses().size());

            CourseOverviewResponse.CourseDetailResponse first = result.allCourses().get(0);
            assertEquals(100L, first.id());
            assertEquals("Safety Course", first.title());
            assertEquals("Learn safety", first.courseDescription());
            assertEquals(1, first.responsibleUsers().size());
            assertEquals("alice", first.responsibleUsers().get(0).legalName());
            assertEquals(2, first.resources().size());
            assertEquals("link", first.resources().get(0).type());
            assertEquals("https://example.com/safety", first.resources().get(0).name());
            assertEquals("file", first.resources().get(1).type());
            assertEquals("safety.pdf", first.resources().get(1).name());

            CourseOverviewResponse.CourseDetailResponse second = result.allCourses().get(1);
            assertEquals(200L, second.id());
            assertEquals("Drink Course", second.title());
            assertEquals("bob", second.responsibleUsers().get(0).legalName());

            // userProgress
            assertEquals(1, result.userProgress().size());
            CourseOverviewResponse.UserProgressOverview userProg = result.userProgress().get(0);
            assertEquals(2L, userProg.userId());
            assertEquals("bob", userProg.legalName());
            assertEquals(2, userProg.courses().size());

            CourseOverviewResponse.UserCourseStatus s1 = userProg.courses().stream()
                    .filter(s -> s.courseId().equals(100L)).findFirst().orElseThrow();
            assertTrue(s1.completed());
            assertEquals("Safety Course", s1.title());

            CourseOverviewResponse.UserCourseStatus s2 = userProg.courses().stream()
                    .filter(s -> s.courseId().equals(200L)).findFirst().orElseThrow();
            assertFalse(s2.completed());
            assertEquals("Drink Course", s2.title());
        }

        @Test
        @DisplayName("returns empty overview when no courses exist")
        void getCourseOverview_empty() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findByOrganizationId(10L)).thenReturn(List.of());
            when(courseUserProgressRepository.findByCourseIdIn(List.of())).thenReturn(List.of());

            CourseOverviewResponse result = courseService.getCourseOverview(10L, 1L);

            assertTrue(result.allCourses().isEmpty());
            assertTrue(result.userProgress().isEmpty());
        }

        @Test
        @DisplayName("throws when user not in org")
        void getCourseOverview_notMember_throws() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> courseService.getCourseOverview(10L, 99L));

            verify(courseRepository, never()).findByOrganizationId(any());
        }
    }

    @Nested
    @DisplayName("addCourseLink")
    class AddCourseLinkTests {

        @Test
        @DisplayName("creates and returns course link")
        void addCourseLink_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseLinkRepository.save(any(CourseLink.class))).thenAnswer(inv -> {
                CourseLink saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 700L);
                return saved;
            });

            CourseLink result = courseService.addCourseLink(100L, "https://example.com/course", 10L, 1L);

            assertEquals(700L, result.getId());
            assertEquals("https://example.com/course", result.getLink());
            assertEquals(testCourse, result.getCourse());
        }
    }

    @Nested
    @DisplayName("removeCourseLink")
    class RemoveCourseLinkTests {

        @Test
        @DisplayName("removes existing course link")
        void removeCourseLink_success() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

            CourseLink link = new CourseLink();
            ReflectionTestUtils.setField(link, "id", 700L);
            link.setCourse(testCourse);
            link.setLink("https://example.com/course");
            when(courseLinkRepository.findByIdAndCourseId(700L, 100L)).thenReturn(Optional.of(link));

            courseService.removeCourseLink(100L, 700L, 10L, 1L);

            verify(courseLinkRepository).delete(link);
        }

        @Test
        @DisplayName("throws when course link is missing")
        void removeCourseLink_notFound_throws() {
            stubOrgMembership(10L, 1L);
            when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
            when(courseLinkRepository.findByIdAndCourseId(700L, 100L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> courseService.removeCourseLink(100L, 700L, 10L, 1L));

            assertEquals("Course link not found", ex.getMessage());
            verify(courseLinkRepository, never()).delete(any(CourseLink.class));
        }
    }
}
