package com.grimni.dto;

import java.util.List;

public record ReplaceRoutineAssignmentsRequest(
    List<Long> verifierUserIds,
    List<Long> deviationReceiverUserIds,
    List<Long> performerUserIds,
    List<Long> deputyUserIds
) {}
