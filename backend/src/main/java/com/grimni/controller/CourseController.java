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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.grimni.domain.Course;
import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.AccessLevel;
import com.grimni.dto.CourseResponse;
import com.grimni.dto.CourseResponsibleUserResponse;
import com.grimni.dto.CourseUserProgressResponse;
import com.grimni.dto.CreateCourseRequest;
import com.grimni.dto.UpdateCourseProgressRequest;
import com.grimni.dto.UpdateCourseRequest;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.security.JwtUserPrinciple;
import com.grimni.service.CourseService;
import com.grimni.service.SimpleStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing organizational training courses and educational materials.
 * <p>
 * This controller serves as the primary interface for:
 * <ul>
 * <li><b>Course Lifecycle:</b> Creation, retrieval, partial updates, and deletion.</li>
 * <li><b>Progress Monitoring:</b> Tracking user completion status and assignment management.</li>
 * <li><b>Responsibility Mapping:</b> Assigning and viewing personnel responsible for course oversight.</li>
 * <li><b>Content Management:</b> Secure upload, download, and deletion of course-related files.</li>
 * </ul>
 * <p>
 * All endpoints enforce strict multi-tenant isolation based on the authenticated user's organization.
 */
@Tag(name = "Courses", description = "Course CRUD, progress tracking, responsible users, and file management")
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

    /**
     * Initializes a new course within the caller's organization.
     *
     * @param request        Validated request containing course details.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the created {@link CourseResponse}.
     */
    @Operation(summary = "Create course")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Course course = courseService.createCourse(request, principal.orgId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse.fromEntity(course));
    }

    /**
     * Retrieves all courses associated with the authenticated user's organization.
     *
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with a list of {@link CourseResponse} objects.
     */
    @Operation(summary = "List courses")
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

    /**
     * Fetches details for a specific course by ID, ensuring organizational access rights.
     *
     * @param courseId       The unique identifier of the course.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the {@link CourseResponse}.
     */
    @Operation(summary = "Get course by ID")
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCourse(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        Course course = courseService.getCourseById(courseId, principal.orgId(), principal.userId());
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    /**
     * Partially updates an existing course definition.
     *
     * @param courseId       The unique identifier of the course.
     * @param request        Validated DTO containing fields to update.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the updated {@link CourseResponse}.
     */
    @Operation(summary = "Update course")
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

    /**
     * Permanently deletes a course and its associated metadata.
     *
     * @param courseId       The unique identifier of the course.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     */
    @Operation(summary = "Delete course")
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId, Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        courseService.deleteCourse(courseId, principal.orgId(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Provides a high-level summary of all courses, including completion metrics for the organization.
     *
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the course overview statistics.
     */
    @Operation(summary = "Get course overview")
    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> getCourseOverview(Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCourseOverview(principal.orgId(), principal.userId()));
    }

    // Course User Progress
    // -------------------------------------------------------------------------

    /**
     * Retrieves progress reports for all users currently assigned to a specific course.
     *
     * @param courseId       The unique identifier of the course.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with a list of {@link CourseUserProgressResponse}.
     */
    @Operation(summary = "List progress by course")
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

    /**
     * Retrieves the progress details of a specific user for a specific course.
     *
     * @param courseId       The unique identifier of the course.
     * @param targetUserId   The unique identifier of the user.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with the {@link CourseUserProgressResponse}.
     */
    @Operation(summary = "Get user progress for course")
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

    /**
     * Enrolls a user in a course and initializes their progress tracking.
     *
     * @param courseId       The unique identifier of the course.
     * @param targetUserId   The unique identifier of the user to assign.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with the initialized {@link CourseUserProgressResponse}.
     */
    @Operation(summary = "Assign user to course")
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

    /**
     * Updates the completion status and progress metrics for a user's course enrollment.
     *
     * @param request        Validated DTO containing progress updates.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with the updated {@link CourseUserProgressResponse}.
     */
    @Operation(summary = "Update course progress", description = "Sets a user's completion status for a course")
    @PutMapping("/course-progress")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateProgress(
            @Valid @RequestBody UpdateCourseProgressRequest request,
            Authentication authentication) {
        JwtUserPrinciple principal = (JwtUserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(CourseUserProgressResponse.fromEntity(
                courseService.updateProgress(request.courseId(), request.userId(), request.completed(), principal.orgId(), principal.userId())));
    }

    /**
     * Unenrolls a user from a course, removing their progress record.
     *
     * @param courseId       The unique identifier of the course.
     * @param targetUserId   The unique identifier of the user to remove.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     */
    @Operation(summary = "Remove user from course")
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

    /**
     * Retrieves the list of users designated as responsible for a specific course.
     *
     * @param courseId       The unique identifier of the course.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with a list of {@link CourseResponsibleUserResponse}.
     */
    @Operation(summary = "List responsible users")
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

    /**
     * Designates a user as a responsible party for a specific course.
     *
     * @param courseId       The unique identifier of the course.
     * @param targetUserId   The unique identifier of the user to designate.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with the created {@link CourseResponsibleUserResponse}.
     */
    @Operation(summary = "Assign responsible user")
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

    /**
     * Removes a user's responsibility status for a specific course.
     *
     * @param courseId       The unique identifier of the course.
     * @param targetUserId   The unique identifier of the user to remove responsibility from.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     */
    @Operation(summary = "Remove responsible user")
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

    /**
     * Processes a file upload and associates it with a specific course.
     * <p>
     * Generates a unique storage key and applies organizational access controls.
     *
     * @param courseId       The unique identifier of the course.
     * @param file           The multipart file to upload.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the ID of the saved {@link FileObject}.
     * @throws ResponseStatusException if the file is empty or if storage/database lookups fail.
     */
    @Operation(summary = "Upload course file")
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

    /**
     * Streams the content of a course-related file for download.
     *
     * @param courseId       The unique identifier of the course.
     * @param fileId         The unique identifier of the file.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} containing the file byte stream and appropriate headers.
     * @throws ResponseStatusException if the file is missing or the user lacks sufficient permissions.
     */
    @Operation(summary = "Download course file")
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

    /**
     * Deletes a specific file associated with a course.
     *
     * @param courseId       The unique identifier of the course.
     * @param fileId         The unique identifier of the file.
     * @param authentication Security context containing the {@link JwtUserPrinciple}.
     * @return {@link ResponseEntity} with HTTP 204 No Content status.
     * @throws ResponseStatusException if the file is missing or the user lacks sufficient permissions.
     */
    @Operation(summary = "Delete course file")
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