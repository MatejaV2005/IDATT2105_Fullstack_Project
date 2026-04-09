package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.Certificate;

public record CertificateResponse(
    Long id,
    String certificateName,
    Long userId,
    Long fileId,
    Long organizationId,
    Long courseId,
    LocalDateTime createdAt
) {
    public static CertificateResponse fromEntity(Certificate cert) {
        return new CertificateResponse(
            cert.getId(),
            cert.getCertificateName(),
            cert.getUser().getId(),
            cert.getFile().getId(),
            cert.getOrganization() != null ? cert.getOrganization().getId() : null,
            cert.getCourse() != null ? cert.getCourse().getId() : null,
            cert.getCreatedAt()
        );
    }
}
