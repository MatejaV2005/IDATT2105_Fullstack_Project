package com.grimni.dto;

import jakarta.validation.constraints.NotNull;

public record SwitchOrganizationRequest(
    @NotNull Long organizationId
) {
}
