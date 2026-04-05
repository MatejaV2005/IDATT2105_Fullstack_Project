package com.grimni.dto;

public record LoginRequest(String username, String password, Long orgId) {
    
}
