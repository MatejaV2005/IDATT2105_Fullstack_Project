package com.grimni.dto;

public record CcpIntervalResponse(
    Long intervalId,
    Long intervalStart,
    Long intervalRepeatTime
) {}
