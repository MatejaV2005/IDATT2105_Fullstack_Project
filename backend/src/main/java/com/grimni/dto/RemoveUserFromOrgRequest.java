package com.grimni.dto;

import jakarta.validation.constraints.NotNull;

public record RemoveUserFromOrgRequest(
    @NotNull Long userId
) {
}
