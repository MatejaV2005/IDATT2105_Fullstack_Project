package com.grimni.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssignedCcpResponse(
    Long ccpId,
    String name,
    String monitoredDescription,
    BigDecimal criticalMin,
    BigDecimal criticalMax,
    String unit,
    String repeatText,
    LocalDateTime dueAt,
    boolean completedForCurrentInterval,
    LocalDateTime completedAt,
    LocalDateTime lastCompletedAt,
    BigDecimal lastMeasuredValue,
    String immediateCorrectiveAction
) {
}
