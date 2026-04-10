package com.grimni.dto;

import com.grimni.domain.enums.OrgUserRole;

import jakarta.validation.constraints.NotNull;

public record UpdateUserOrgRoleRequest(
    @NotNull(message = "userId is required")
    Long userId,

    @NotNull(message = "role is required")
    OrgUserRole role
) {
}
