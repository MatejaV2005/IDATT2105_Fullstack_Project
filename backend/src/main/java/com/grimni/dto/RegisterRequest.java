package com.grimni.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "email field can not be blank")
    @Email(message = "must be a valid email address")
    String email, 

    @NotBlank(message = "password field can not be blank")
    String password, 

    @NotBlank(message = "name field can not be blank")
    String legalName) {

    
}
