package com.grimni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimni.controller.CourseController;
import com.grimni.domain.Course;
import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.security.SecurityConfig;
import com.grimni.service.CourseService;
import com.grimni.util.JwtAuthFilter;
import com.grimni.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Organization testOrg;
    private Course testCourse;
    private User targetUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        testOrg = new Organization();
        ReflectionTestUtils.setField(testOrg, "id", 10L);
        testOrg.setOrgName("Test Org");

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Safety Course");
        testCourse.setCourseDescription("Learn safety");
        testCourse.setOrganization(testOrg);

        targetUser = new User();
        ReflectionTestUtils.setField(targetUser, "id", 2L);
        targetUser.setLegalName("bob");
    }

    private static UsernamePasswordAuthenticationToken authWithRole(String role) {
        JwtUserPrinciple principal = new JwtUserPrinciple(1L, 10L, "alice", role);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    // =========================================================================
    // CRUD Endpoints
    // =========================================================================

    @Nested
    @DisplayName("POST /courses")
    class CreateCourseTests {

        @Test
        @DisplayName("OWNER can create — returns 201")
        void createCourse_owner_returns201() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest("Safety Course", "Learn safety");
            when(courseService.createCourse(any(CreateCourseRequest.class), eq(10L), eq(1L)))
                    .thenReturn(testCourse);

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.title").value("Safety Course"))
                    .andExpect(jsonPath("$.courseDescription").value("Learn safety"))
                    .andExpect(jsonPath("$.organizationId").value(10));
        }

        @Test
        @DisplayName("MANAGER can create — returns 201")
        void createCourse_manager_returns201() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest("Safety Course", "Learn safety");
            when(courseService.createCourse(any(CreateCourseRequest.class), eq(10L), eq(1L)))
                    .thenReturn(testCourse);

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("MANAGER")))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("WORKER cannot create — returns 403")
        void createCourse_worker_returns403() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest("Title", "Desc");

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).createCourse(any(), any(), any());
        }

        @Test
        @DisplayName("unauthenticated — returns 403")
        void createCourse_unauthenticated_returns403() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest("Title", "Desc");

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).createCourse(any(), any(), any());
        }

        @Test
        @DisplayName("service throws EntityNotFound — returns 404")
        void createCourse_orgNotFound_returns404() throws Exception {
            CreateCourseRequest request = new CreateCourseRequest("Title", "Desc");
            when(courseService.createCourse(any(), eq(10L), eq(1L)))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Organization not found"));
        }
    }

    @Nested
    @DisplayName("GET /courses")
    class GetCoursesTests {

        @Test
        @DisplayName("any authenticated user — returns 200 with courses")
        void getCourses_authenticated_returns200() throws Exception {
            when(courseService.getCoursesByOrg(10L, 1L)).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/courses")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(100))
                    .andExpect(jsonPath("$[0].title").value("Safety Course"));
        }

        @Test
        @DisplayName("returns empty list when no courses")
        void getCourses_empty_returns200() throws Exception {
            when(courseService.getCoursesByOrg(10L, 1L)).thenReturn(List.of());

            mockMvc.perform(get("/courses")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated — returns 403")
        void getCourses_unauthenticated_returns403() throws Exception {
            mockMvc.perform(get("/courses"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /courses/{courseId}")
    class GetCourseByIdTests {

        @Test
        @DisplayName("returns 200 with course details")
        void getCourse_success() throws Exception {
            when(courseService.getCourseById(100L, 10L, 1L)).thenReturn(testCourse);

            mockMvc.perform(get("/courses/100")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.title").value("Safety Course"))
                    .andExpect(jsonPath("$.courseDescription").value("Learn safety"))
                    .andExpect(jsonPath("$.organizationId").value(10));
        }

        @Test
        @DisplayName("returns 404 when course not found")
        void getCourse_notFound_returns404() throws Exception {
            when(courseService.getCourseById(999L, 10L, 1L))
                    .thenThrow(new EntityNotFoundException("Course not found"));

            mockMvc.perform(get("/courses/999")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Course not found"));
        }
    }

    @Nested
    @DisplayName("PATCH /courses/{courseId}")
    class UpdateCourseTests {

        @Test
        @DisplayName("OWNER can update — returns 200")
        void updateCourse_owner_returns200() throws Exception {
            Course updated = new Course();
            updated.setId(100L);
            updated.setTitle("Updated Title");
            updated.setCourseDescription("Learn safety");
            updated.setOrganization(testOrg);

            UpdateCourseRequest request = new UpdateCourseRequest("Updated Title", null);
            when(courseService.updateCourse(eq(100L), any(UpdateCourseRequest.class), eq(10L), eq(1L)))
                    .thenReturn(updated);

            mockMvc.perform(patch("/courses/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));
        }

        @Test
        @DisplayName("WORKER cannot update — returns 403")
        void updateCourse_worker_returns403() throws Exception {
            UpdateCourseRequest request = new UpdateCourseRequest("Title", null);

            mockMvc.perform(patch("/courses/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).updateCourse(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /courses/{courseId}")
    class DeleteCourseTests {

        @Test
        @DisplayName("OWNER can delete — returns 204")
        void deleteCourse_owner_returns204() throws Exception {
            mockMvc.perform(delete("/courses/100")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(courseService).deleteCourse(100L, 10L, 1L);
        }

        @Test
        @DisplayName("WORKER cannot delete — returns 403")
        void deleteCourse_worker_returns403() throws Exception {
            mockMvc.perform(delete("/courses/100")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).deleteCourse(any(), any(), any());
        }

        @Test
        @DisplayName("returns 404 when course not found")
        void deleteCourse_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException("Course not found"))
                    .when(courseService).deleteCourse(999L, 10L, 1L);

            mockMvc.perform(delete("/courses/999")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Course not found"));
        }
    }

    // =========================================================================
    // Progress Endpoints
    // =========================================================================

    @Nested
    @DisplayName("GET /courses/{courseId}/progress")
    class GetProgressByCourseTests {

        @Test
        @DisplayName("OWNER gets progress list — returns 200")
        void getProgress_owner_returns200() throws Exception {
            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(true);
            when(courseService.getProgressByCourse(100L, 10L, 1L)).thenReturn(List.of(p));

            mockMvc.perform(get("/courses/100/progress")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].courseId").value(100))
                    .andExpect(jsonPath("$[0].userId").value(2))
                    .andExpect(jsonPath("$[0].isCompleted").value(true));
        }

        @Test
        @DisplayName("WORKER cannot view all progress — returns 403")
        void getProgress_worker_returns403() throws Exception {
            mockMvc.perform(get("/courses/100/progress")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).getProgressByCourse(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /courses/{courseId}/progress/{targetUserId}")
    class GetProgressForUserTests {

        @Test
        @DisplayName("any authenticated user can view — returns 200")
        void getProgressForUser_worker_returns200() throws Exception {
            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(false);
            when(courseService.getProgressForUser(100L, 2L, 10L, 1L)).thenReturn(p);

            mockMvc.perform(get("/courses/100/progress/2")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(100))
                    .andExpect(jsonPath("$.userId").value(2))
                    .andExpect(jsonPath("$.isCompleted").value(false));
        }

        @Test
        @DisplayName("returns 404 when progress not found")
        void getProgressForUser_notFound_returns404() throws Exception {
            when(courseService.getProgressForUser(100L, 99L, 10L, 1L))
                    .thenThrow(new EntityNotFoundException("Progress not found"));

            mockMvc.perform(get("/courses/100/progress/99")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Progress not found"));
        }
    }

    @Nested
    @DisplayName("POST /courses/{courseId}/progress/{targetUserId}")
    class AssignUserToCourseTests {

        @Test
        @DisplayName("OWNER can assign — returns 201")
        void assignUser_owner_returns201() throws Exception {
            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(false);
            when(courseService.assignUserToCourse(100L, 2L, 10L, 1L)).thenReturn(p);

            mockMvc.perform(post("/courses/100/progress/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.courseId").value(100))
                    .andExpect(jsonPath("$.userId").value(2))
                    .andExpect(jsonPath("$.isCompleted").value(false));
        }

        @Test
        @DisplayName("WORKER cannot assign — returns 403")
        void assignUser_worker_returns403() throws Exception {
            mockMvc.perform(post("/courses/100/progress/2")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).assignUserToCourse(any(), any(), any(), any());
        }

        @Test
        @DisplayName("duplicate assignment — returns 400")
        void assignUser_duplicate_returns400() throws Exception {
            when(courseService.assignUserToCourse(100L, 2L, 10L, 1L))
                    .thenThrow(new IllegalArgumentException("User is already assigned to this course"));

            mockMvc.perform(post("/courses/100/progress/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("User is already assigned to this course"));
        }
    }

    @Nested
    @DisplayName("PATCH /courses/{courseId}/progress/{targetUserId}")
    class UpdateProgressTests {

        @Test
        @DisplayName("OWNER can update progress — returns 200")
        void updateProgress_owner_returns200() throws Exception {
            CourseUserProgress p = new CourseUserProgress();
            p.setCourse(testCourse);
            p.setUser(targetUser);
            p.setIsCompleted(true);
            when(courseService.updateProgress(100L, 2L, true, 10L, 1L)).thenReturn(p);

            mockMvc.perform(patch("/courses/100/progress/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("true")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isCompleted").value(true));
        }

        @Test
        @DisplayName("WORKER cannot update progress — returns 403")
        void updateProgress_worker_returns403() throws Exception {
            mockMvc.perform(patch("/courses/100/progress/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("true")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).updateProgress(any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /courses/{courseId}/progress/{targetUserId}")
    class RemoveUserFromCourseTests {

        @Test
        @DisplayName("OWNER can remove — returns 204")
        void removeUser_owner_returns204() throws Exception {
            mockMvc.perform(delete("/courses/100/progress/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(courseService).removeUserFromCourse(100L, 2L, 10L, 1L);
        }

        @Test
        @DisplayName("WORKER cannot remove — returns 403")
        void removeUser_worker_returns403() throws Exception {
            mockMvc.perform(delete("/courses/100/progress/2")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).removeUserFromCourse(any(), any(), any(), any());
        }
    }

    // =========================================================================
    // Responsible User Endpoints
    // =========================================================================

    @Nested
    @DisplayName("GET /courses/{courseId}/responsible")
    class GetResponsibleUsersTests {

        @Test
        @DisplayName("OWNER gets responsible users — returns 200")
        void getResponsible_owner_returns200() throws Exception {
            CourseResponsibleUser r = new CourseResponsibleUser();
            r.setCourse(testCourse);
            r.setUser(targetUser);
            when(courseService.getResponsibleUsers(100L, 10L, 1L)).thenReturn(List.of(r));

            mockMvc.perform(get("/courses/100/responsible")
                            .with(authentication(authWithRole("OWNER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].courseId").value(100))
                    .andExpect(jsonPath("$[0].userId").value(2));
        }

        @Test
        @DisplayName("WORKER cannot view responsible users — returns 403")
        void getResponsible_worker_returns403() throws Exception {
            mockMvc.perform(get("/courses/100/responsible")
                            .with(authentication(authWithRole("WORKER"))))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).getResponsibleUsers(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("POST /courses/{courseId}/responsible/{targetUserId}")
    class AssignResponsibleUserTests {

        @Test
        @DisplayName("OWNER can assign responsible — returns 201")
        void assignResponsible_owner_returns201() throws Exception {
            CourseResponsibleUser r = new CourseResponsibleUser();
            r.setCourse(testCourse);
            r.setUser(targetUser);
            when(courseService.assignResponsibleUser(100L, 2L, 10L, 1L)).thenReturn(r);

            mockMvc.perform(post("/courses/100/responsible/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.courseId").value(100))
                    .andExpect(jsonPath("$.userId").value(2));
        }

        @Test
        @DisplayName("WORKER cannot assign responsible — returns 403")
        void assignResponsible_worker_returns403() throws Exception {
            mockMvc.perform(post("/courses/100/responsible/2")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).assignResponsibleUser(any(), any(), any(), any());
        }

        @Test
        @DisplayName("duplicate responsible — returns 400")
        void assignResponsible_duplicate_returns400() throws Exception {
            when(courseService.assignResponsibleUser(100L, 2L, 10L, 1L))
                    .thenThrow(new IllegalArgumentException("User is already a responsible user for this course"));

            mockMvc.perform(post("/courses/100/responsible/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("User is already a responsible user for this course"));
        }
    }

    @Nested
    @DisplayName("DELETE /courses/{courseId}/responsible/{targetUserId}")
    class RemoveResponsibleUserTests {

        @Test
        @DisplayName("OWNER can remove responsible — returns 204")
        void removeResponsible_owner_returns204() throws Exception {
            mockMvc.perform(delete("/courses/100/responsible/2")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(courseService).removeResponsibleUser(100L, 2L, 10L, 1L);
        }

        @Test
        @DisplayName("WORKER cannot remove responsible — returns 403")
        void removeResponsible_worker_returns403() throws Exception {
            mockMvc.perform(delete("/courses/100/responsible/2")
                            .with(authentication(authWithRole("WORKER")))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courseService, never()).removeResponsibleUser(any(), any(), any(), any());
        }

        @Test
        @DisplayName("returns 404 when responsible user not found")
        void removeResponsible_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException("Responsible user not found"))
                    .when(courseService).removeResponsibleUser(100L, 99L, 10L, 1L);

            mockMvc.perform(delete("/courses/100/responsible/99")
                            .with(authentication(authWithRole("OWNER")))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Responsible user not found"));
        }
    }
}
