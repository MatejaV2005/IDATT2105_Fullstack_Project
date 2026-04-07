package com.grimni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.Course;
import com.grimni.dto.CourseResponse;
import com.grimni.dto.CourseResponsibleUserResponse;
import com.grimni.dto.CourseUserProgressResponse;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Course course = courseService.createCourse(request, principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse.fromEntity(course));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCourses(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CourseResponse> courses = courseService.getCoursesByOrg(principal.orgId(), principal.userId())
                .stream()
                .map(CourseResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCourse(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Course course = courseService.getCourseById(courseId, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    @PatchMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Course course = courseService.updateCourse(courseId, request, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        courseService.deleteCourse(courseId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Course User Progress
    // -------------------------------------------------------------------------

    @GetMapping("/{courseId}/progress")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getProgressByCourse(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CourseUserProgressResponse> progress = courseService.getProgressByCourse(courseId, principal.orgId(), principal.userId())
                .stream()
                .map(CourseUserProgressResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{courseId}/progress/{targetUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProgressForUser(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(CourseUserProgressResponse.fromEntity(
                courseService.getProgressForUser(courseId, targetUserId, principal.orgId(), principal.userId())));
    }

    @PostMapping("/{courseId}/progress/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> assignUserToCourse(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseUserProgressResponse.fromEntity(
                courseService.assignUserToCourse(courseId, targetUserId, principal.orgId(), principal.userId())));
    }

    @PatchMapping("/{courseId}/progress/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            @RequestBody Boolean isCompleted,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(CourseUserProgressResponse.fromEntity(
                courseService.updateProgress(courseId, targetUserId, isCompleted, principal.orgId(), principal.userId())));
    }

    @DeleteMapping("/{courseId}/progress/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> removeUserFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        courseService.removeUserFromCourse(courseId, targetUserId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Course Responsible Users
    // -------------------------------------------------------------------------

    @GetMapping("/{courseId}/responsible")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getResponsibleUsers(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        List<CourseResponsibleUserResponse> responsible = courseService.getResponsibleUsers(courseId, principal.orgId(), principal.userId())
                .stream()
                .map(CourseResponsibleUserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responsible);
    }

    @PostMapping("/{courseId}/responsible/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> assignResponsibleUser(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponsibleUserResponse.fromEntity(
                courseService.assignResponsibleUser(courseId, targetUserId, principal.orgId(), principal.userId())));
    }

    @DeleteMapping("/{courseId}/responsible/{targetUserId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> removeResponsibleUser(
            @PathVariable Long courseId,
            @PathVariable Long targetUserId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        courseService.removeResponsibleUser(courseId, targetUserId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }
}
