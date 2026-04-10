package com.grimni.dto;

import com.grimni.domain.OrgUserBridge;

public record MyOrganizationMembershipResponse(
    Long id,
    String orgName,
    String orgAddress,
    int orgNumber,
    boolean alcoholEnabled,
    boolean foodEnabled,
    String orgRole,
    boolean isCurrent
) {
    public static MyOrganizationMembershipResponse fromEntity(OrgUserBridge bridge, Long currentOrgId) {
        return new MyOrganizationMembershipResponse(
            bridge.getOrganization().getId(),
            bridge.getOrganization().getOrgName(),
            bridge.getOrganization().getOrgAddress(),
            bridge.getOrganization().getOrgNumber(),
            bridge.getOrganization().getAlcoholEnabled(),
            bridge.getOrganization().getFoodEnabled(),
            bridge.getUserRole().name(),
            bridge.getOrganization().getId().equals(currentOrgId)
        );
    }
}
