package com.grimni.dto;

public record TeamAssignmentsResponse(
    int verifier,
    int deviationReceiver,
    int performer,
    int deputy
) {
}
