package com.grimni.dto;

public record MappingPointResponse(
    Long id,
    String law,
    Short dots,
    String title,
    String challenges,
    String measures,
    String responsibleText
) {}
