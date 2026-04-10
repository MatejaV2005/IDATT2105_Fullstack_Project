package com.grimni.dto;

import com.grimni.domain.enums.OrgUserRole;

import jakarta.validation.constraints.NotNull;

public record AddUserToOrgRequest(
    @NotNull Long userId,
    @NotNull OrgUserRole role
) {
}
