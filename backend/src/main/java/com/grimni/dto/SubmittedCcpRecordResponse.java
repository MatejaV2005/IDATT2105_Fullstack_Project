package com.grimni.dto;

public record SubmittedCcpRecordResponse(
    AssignedCcpResponse ccp,
    Long recordId,
    boolean outsideCriticalRange
) {
}
