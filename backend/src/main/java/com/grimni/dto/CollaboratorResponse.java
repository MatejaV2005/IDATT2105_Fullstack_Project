package com.grimni.dto;

import com.grimni.domain.User;

public record CollaboratorResponse(Long userId, String legalName) {
    public static CollaboratorResponse fromEntity(User user) {
        return new CollaboratorResponse(user.getId(), user.getLegalName());
    }
    
}
