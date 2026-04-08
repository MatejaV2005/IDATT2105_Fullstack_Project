package com.grimni.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateCcpRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "How cannot be blank")
    String how,

    @NotBlank(message = "Equipment cannot be blank")
    String equipment,

    @NotBlank(message = "Instructions and calibration cannot be blank")
    String instructionsAndCalibration,

    @NotBlank(message = "Immediate corrective action cannot be blank")
    String immediateCorrectiveAction,

    @NotNull(message = "Critical minimum is required")
    BigDecimal criticalMin,

    @NotNull(message = "Critical maximum is required")
    BigDecimal criticalMax,

    String unit,

    String monitoredDescription,

    @NotNull(message = "Interval start is required")
    @PositiveOrZero(message = "Interval start must be zero or positive")
    Long intervalStart,

    @NotNull(message = "Interval repeat time is required")
    @Positive(message = "Interval repeat time must be positive")
    Long intervalRepeatTime,

    List<Long> verifierUserIds,
    List<Long> deviationReceiverUserIds,
    List<Long> performerUserIds,
    List<Long> deputyUserIds,

    List<@Valid CreateCcpCorrectiveMeasureRequest> correctiveMeasures
) {}
