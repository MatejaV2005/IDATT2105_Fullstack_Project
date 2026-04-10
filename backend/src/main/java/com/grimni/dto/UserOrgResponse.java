package com.grimni.dto;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.enums.OrgUserRole;

public record UserOrgResponse(
    Long id,
    String legalName,
    String email,
    OrgUserRole accessLevel
) {
    public static UserOrgResponse fromEntity(OrgUserBridge bridge) {
        return new UserOrgResponse(bridge.getUser().getId(), 
        bridge.getUser().getLegalName(),
        bridge.getUser().getEmail(), 
        bridge.getUserRole());
    }
}
