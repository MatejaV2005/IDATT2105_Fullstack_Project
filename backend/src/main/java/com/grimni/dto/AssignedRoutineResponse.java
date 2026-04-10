package com.grimni.dto;

import java.time.LocalDateTime;

public record AssignedRoutineResponse(
    Long routineId,
    String title,
    String categoryName,
    String description,
    String immediateCorrectiveAction,
    LocalDateTime dueAt,
    boolean completedForCurrentInterval,
    LocalDateTime completedAt,
    LocalDateTime lastCompletedAt
) {
}
