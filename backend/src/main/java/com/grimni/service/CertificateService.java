package com.grimni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grimni.domain.Certificate;
import com.grimni.domain.Course;
import com.grimni.domain.FileObject;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CreateCertificateRequest;
import com.grimni.dto.UpdateCertificateRequest;
import com.grimni.repository.CertificateRepository;
import com.grimni.repository.CourseRepository;
import com.grimni.repository.FileObjectRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing organizational certificates and professional credentials.
 * <p>
 * This service handles the business logic for verifying user-to-organization relationships,
 * linking certificates to specific training courses, and managing associated file attachments.
 * All operations are scoped by organization ID to ensure multi-tenant data isolation.
 */
@Service
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final FileObjectRepository fileObjectRepository;
    private final OrganizationRepository organizationRepository;
    private final CourseRepository courseRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;

    public CertificateService(CertificateRepository certificateRepository,
                              UserRepository userRepository,
                              FileObjectRepository fileObjectRepository,
                              OrganizationRepository organizationRepository,
                              CourseRepository courseRepository,
                              OrgUserBridgeRepository orgUserBridgeRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.organizationRepository = organizationRepository;
        this.courseRepository = courseRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
    }

    /**
     * Validates that a specific user holds an active membership within an organization.
     *
     * @param orgId the unique identifier of the organization.
     * @param userId the unique identifier of the user to validate.
     * @throws EntityNotFoundException if the user-organization bridge does not exist.
     */
    private void validateUserBelongsToOrg(Long orgId, Long userId) {
        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("User {} does not belong to organization {}", userId, orgId);
                    return new EntityNotFoundException("Organization not found");
                });
    }

    /**
     * Creates and persists a new certificate for a target user within an organization.
     *
     * @param request the DTO containing certificate name, target user, and file information.
     * @param orgId the organization ID where the certificate will be stored.
     * @param userId the ID of the user performing the creation.
     * @return the persisted {@link Certificate} entity.
     * @throws EntityNotFoundException if the organization, target user, or file reference is missing.
     */
    public Certificate createCertificate(CreateCertificateRequest request, Long orgId, Long userId) {
        logger.info("Creating certificate '{}' in organization {} by user {}", request.certificateName(), orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    logger.warn("Create certificate failed: organization {} not found", orgId);
                    return new EntityNotFoundException("Organization not found");
                });

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    logger.warn("Create certificate failed: user {} not found", request.userId());
                    return new EntityNotFoundException("User not found");
                });

        FileObject file = fileObjectRepository.findById(request.fileId())
                .orElseThrow(() -> {
                    logger.warn("Create certificate failed: file {} not found", request.fileId());
                    return new EntityNotFoundException("File not found");
                });

        Course course = null;
        if (request.courseId() != null) {
            course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> {
                        logger.warn("Create certificate failed: course {} not found", request.courseId());
                        return new EntityNotFoundException("Course not found");
                    });
        }

        Certificate cert = new Certificate();
        cert.setCertificateName(request.certificateName());
        cert.setUser(targetUser);
        cert.setFile(file);
        cert.setOrganization(org);
        cert.setCourse(course);
        cert = certificateRepository.save(cert);

        logger.info("Certificate '{}' (id={}) created in organization {}", cert.getCertificateName(), cert.getId(), orgId);
        return cert;
    }

    /**
     * Retrieves all certificates associated with a specific user across all organizations.
     *
     * @param userId the unique identifier of the user.
     * @return a list of {@link Certificate} entities.
     */
    public List<Certificate> getCertificatesForUser(Long userId) {
        logger.info("Fetching certificates for user {}", userId);
        List<Certificate> certs = certificateRepository.findByUserId(userId);
        logger.info("Found {} certificates for user {}", certs.size(), userId);
        return certs;
    }

    /**
     * Retrieves all certificates belonging to a specific organization.
     *
     * @param orgId the unique identifier of the organization.
     * @param userId the ID of the user performing the query (used for membership validation).
     * @return a list of all {@link Certificate} entities within the organization scope.
     */
    public List<Certificate> getCertificatesForOrg(Long orgId, Long userId) {
        logger.info("Fetching certificates for organization {} by user {}", orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        List<Certificate> certs = certificateRepository.findByOrganizationId(orgId);
        logger.info("Found {} certificates for organization {}", certs.size(), orgId);
        return certs;
    }

    /**
     * Fetches a single certificate by its ID, ensuring it belongs to the specified organization.
     *
     * @param certId the unique identifier of the certificate.
     * @param orgId the organization ID for scope validation.
     * @param userId the ID of the user performing the request.
     * @return the {@link Certificate} entity.
     * @throws EntityNotFoundException if the certificate is missing or belongs to a different organization.
     */
    public Certificate getCertificateById(Long certId, Long orgId, Long userId) {
        logger.info("Fetching certificate {} in organization {} by user {}", certId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Certificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> {
                    logger.warn("Certificate {} not found", certId);
                    return new EntityNotFoundException("Certificate not found");
                });

        if (cert.getOrganization() == null || !cert.getOrganization().getId().equals(orgId)) {
            logger.warn("Certificate {} does not belong to organization {}", certId, orgId);
            throw new EntityNotFoundException("Certificate not found");
        }

        return cert;
    }

    /**
     * Removes a certificate record after validating organizational ownership.
     *
     * @param certId the unique identifier of the certificate.
     * @param orgId the organization ID for scope validation.
     * @param userId the ID of the user performing the deletion.
     * @throws EntityNotFoundException if the certificate is missing or ownership validation fails.
     */
    public void deleteCertificate(Long certId, Long orgId, Long userId) {
        logger.info("Deleting certificate {} in organization {} by user {}", certId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Certificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> {
                    logger.warn("Delete failed: certificate {} not found", certId);
                    return new EntityNotFoundException("Certificate not found");
                });

        if (cert.getOrganization() == null || !cert.getOrganization().getId().equals(orgId)) {
            logger.warn("Certificate {} does not belong to organization {}", certId, orgId);
            throw new EntityNotFoundException("Certificate not found");
        }

        certificateRepository.delete(cert);
        logger.info("Certificate {} deleted from organization {}", certId, orgId);
    }

    /**
     * Retrieves all certificates assigned to a target user within a specific organization.
     *
     * @param targetUserId the ID of the user whose certificates are being retrieved.
     * @param orgId the organization ID.
     * @param userId the ID of the requesting user (used for validation).
     * @return a list of filtered {@link Certificate} entities.
     */
    public List<Certificate> getCertificatesForUserInOrg(Long targetUserId, Long orgId, Long userId) {
        logger.info("Fetching certificates for user {} in organization {} by user {}", targetUserId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);
        validateUserBelongsToOrg(orgId, targetUserId);

        List<Certificate> certs = certificateRepository.findByUserIdAndOrganizationId(targetUserId, orgId);
        logger.info("Found {} certificates for user {} in organization {}", certs.size(), targetUserId, orgId);
        return certs;
    }

    /**
     * Updates an existing certificate's attributes, including metadata and file associations.
     *
     * @param certId the unique identifier of the certificate to update.
     * @param request DTO containing updated fields (null fields are ignored).
     * @param orgId the organization ID for scope validation.
     * @param userId the ID of the user performing the update.
     * @return the updated {@link Certificate} entity.
     * @throws EntityNotFoundException if the certificate, file, or course reference is not found.
     */
    public Certificate updateCertificate(Long certId, UpdateCertificateRequest request, Long orgId, Long userId) {
        logger.info("Updating certificate {} in organization {} by user {}", certId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        Certificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> {
                    logger.warn("Update failed: certificate {} not found", certId);
                    return new EntityNotFoundException("Certificate not found");
                });

        if (cert.getOrganization() == null || !cert.getOrganization().getId().equals(orgId)) {
            logger.warn("Certificate {} does not belong to organization {}", certId, orgId);
            throw new EntityNotFoundException("Certificate not found");
        }

        if (request.certificateName() != null) cert.setCertificateName(request.certificateName());

        if (request.fileId() != null) {
            FileObject file = fileObjectRepository.findById(request.fileId())
                    .orElseThrow(() -> {
                        logger.warn("Update failed: file {} not found", request.fileId());
                        return new EntityNotFoundException("File not found");
                    });
            cert.setFile(file);
        }

        if (request.courseId() != null) {
            Course course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> {
                        logger.warn("Update failed: course {} not found", request.courseId());
                        return new EntityNotFoundException("Course not found");
                    });
            cert.setCourse(course);
        }

        cert = certificateRepository.save(cert);
        logger.info("Certificate {} updated in organization {}", certId, orgId);
        return cert;
    }
}