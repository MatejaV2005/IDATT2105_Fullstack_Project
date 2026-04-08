package com.grimni.dto;

import com.grimni.domain.User;

public record UserResponse(
    Long id,
    String email,
    String legalName
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getLegalName()
        );
    }
}