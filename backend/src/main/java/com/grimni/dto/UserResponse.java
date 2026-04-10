package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.User;

public record UserResponse(
    Long id,
    String email,
    String legalName,
    LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getLegalName(),
            user.getCreatedAt()
        );
    }
}