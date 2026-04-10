package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.Organization;

public record OrganizationResponse(
    Long id,
    String orgName,
    String orgAddress,
    int orgNumber,
    boolean alcoholEnabled,
    boolean foodEnabled,
    LocalDateTime createdAt
) {
    public static OrganizationResponse fromEntity(Organization org) {
        return new OrganizationResponse(
            org.getId(),
            org.getOrgName(),
            org.getOrgAddress(),
            org.getOrgNumber(),
            org.getAlcoholEnabled(),
            org.getFoodEnabled(),
            org.getCreatedAt()
        );
    }
}
