package com.grimni.dto;

import com.grimni.domain.User;

public record UserDirectoryResponse(
    Long id,
    String legalName,
    String email
) {
    public static UserDirectoryResponse fromEntity(User user) {
        return new UserDirectoryResponse(
            user.getId(),
            user.getLegalName(),
            user.getEmail()
        );
    }
}
