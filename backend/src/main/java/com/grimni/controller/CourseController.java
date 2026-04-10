package com.grimni.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.grimni.domain.Course;
import com.grimni.domain.CourseLink;
import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.dto.CourseLinkResponse;
import com.grimni.dto.CourseResponse;
import com.grimni.dto.CourseResponsibleUserResponse;
import com.grimni.dto.CourseUserProgressResponse;
import com.grimni.dto.CreateCourseLinkRequest;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CourseService;
import com.grimni.service.SimpleStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final SimpleStorageService simpleStorageService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public CourseController(CourseService courseService,
                            SimpleStorageService simpleStorageService,
                            UserRepository userRepository,
                            OrganizationRepository organizationRepository) {
        this.courseService = courseService;
        this.simpleStorageService = simpleStorageService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
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

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getCourseOverview(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCourseOverview(principal.orgId(), principal.userId()));
    }

    @PostMapping("/{courseId}/links")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> addCourseLink(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCourseLinkRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        CourseLink courseLink = courseService.addCourseLink(courseId, request.link(), principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseLinkResponse.fromEntity(courseLink));
    }

    @DeleteMapping("/{courseId}/links/{linkId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> removeCourseLink(
            @PathVariable Long courseId,
            @PathVariable Long linkId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        courseService.removeCourseLink(courseId, linkId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }

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

    // -------------------------------------------------------------------------
    // Course Files
    // -------------------------------------------------------------------------

    @PostMapping("/{courseId}/files")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> uploadCourseFile(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Long orgId = principal.orgId();
        Long userId = principal.userId();

        courseService.getCourseById(courseId, orgId, userId);

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        User uploadedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        String originalFilename = file.getOriginalFilename() == null
                ? "unnamed" : StringUtils.cleanPath(file.getOriginalFilename());
        String key = "courses/" + courseId + "/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            FileObject savedFile = simpleStorageService.upload(
                    key,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType(),
                    AccessLevel.ANYONE_IN_ORG,
                    AccessLevel.MANAGER,
                    uploadedBy,
                    originalFilename,
                    organization
            );
            simpleStorageService.linkFileToCourse(savedFile.getId(), courseId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFile.getId());
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", exception);
        }
    }

    @GetMapping("/{courseId}/files/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadCourseFile(
            @PathVariable Long courseId,
            @PathVariable Long fileId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();

        courseService.getCourseById(courseId, principal.orgId(), principal.userId());

        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        try {
            SimpleStorageService.StoredFile storedFile = simpleStorageService.read(fileId, user.getOrganizations());
            FileObject fileObject = storedFile.fileObject();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(storedFile.contentType()))
                    .header("Content-Disposition", "attachment; filename=\"" + fileObject.getFileName() + "\"")
                    .body(storedFile.bytes());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", exception);
        } catch (SecurityException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        }
    }

    @DeleteMapping("/{courseId}/files/{fileId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCourseFile(
            @PathVariable Long courseId,
            @PathVariable Long fileId,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();

        courseService.getCourseById(courseId, principal.orgId(), principal.userId());

        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        try {
            simpleStorageService.delete(fileId, user.getOrganizations());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", exception);
        } catch (SecurityException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        }
    }
}
