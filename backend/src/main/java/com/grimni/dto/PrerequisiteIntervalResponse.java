package com.grimni.dto;

public record PrerequisiteIntervalResponse(
    Long intervalId,
    Long intervalStart,
    Long intervalRepeatTime
) {}
