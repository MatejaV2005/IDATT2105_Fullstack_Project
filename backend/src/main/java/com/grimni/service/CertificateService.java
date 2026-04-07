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

    private void validateUserBelongsToOrg(Long orgId, Long userId) {
        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> {
                    logger.warn("User {} does not belong to organization {}", userId, orgId);
                    return new EntityNotFoundException("Organization not found");
                });
    }

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

    public List<Certificate> getCertificatesForUser(Long userId) {
        logger.info("Fetching certificates for user {}", userId);
        List<Certificate> certs = certificateRepository.findByUserId(userId);
        logger.info("Found {} certificates for user {}", certs.size(), userId);
        return certs;
    }

    public List<Certificate> getCertificatesForOrg(Long orgId, Long userId) {
        logger.info("Fetching certificates for organization {} by user {}", orgId, userId);

        validateUserBelongsToOrg(orgId, userId);

        List<Certificate> certs = certificateRepository.findByOrganizationId(orgId);
        logger.info("Found {} certificates for organization {}", certs.size(), orgId);
        return certs;
    }

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

    public List<Certificate> getCertificatesForUserInOrg(Long targetUserId, Long orgId, Long userId) {
        logger.info("Fetching certificates for user {} in organization {} by user {}", targetUserId, orgId, userId);

        validateUserBelongsToOrg(orgId, userId);
        validateUserBelongsToOrg(orgId, targetUserId);

        List<Certificate> certs = certificateRepository.findByUserIdAndOrganizationId(targetUserId, orgId);
        logger.info("Found {} certificates for user {} in organization {}", certs.size(), targetUserId, orgId);
        return certs;
    }

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
