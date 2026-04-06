package com.grimni.dto;

public record LoginRequest(String email, String password, Long orgId) {
    
}
