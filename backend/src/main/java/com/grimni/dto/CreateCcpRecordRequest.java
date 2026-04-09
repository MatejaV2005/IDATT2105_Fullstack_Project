package com.grimni.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record CreateCcpRecordRequest(
    @NotNull BigDecimal measuredValue,
    String comment
) {
}
