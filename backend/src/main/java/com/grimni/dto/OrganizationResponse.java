package com.grimni.dto;

import com.grimni.domain.Organization;

public record OrganizationResponse(
    Long id,
    String orgName,
    String orgAddress,
    int orgNumber,
    boolean alcoholEnabled,
    boolean foodEnabled
) {
    public static OrganizationResponse fromEntity(Organization org) {
        return new OrganizationResponse(
            org.getId(),
            org.getOrgName(),
            org.getOrgAddress(),
            org.getOrgNumber(),
            org.getAlcoholEnabled(),
            org.getFoodEnabled()
        );
    }
}