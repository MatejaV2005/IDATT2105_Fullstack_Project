package com.grimni.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 1, message = "Legal name cannot be empty")
    String legalName,

    @Size(min = 1, message = "Email cannot be empty")
    @Email(message = "Must be a valid email address")
    String email
) {}