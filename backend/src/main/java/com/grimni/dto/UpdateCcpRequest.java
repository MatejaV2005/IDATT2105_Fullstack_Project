package com.grimni.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateCcpRequest(
    @Size(min = 1, message = "Name cannot be empty")
    String name,

    @Size(min = 1, message = "How cannot be empty")
    String how,

    @Size(min = 1, message = "Equipment cannot be empty")
    String equipment,

    @Size(min = 1, message = "Instructions and calibration cannot be empty")
    String instructionsAndCalibration,

    @Size(min = 1, message = "Immediate corrective action cannot be empty")
    String immediateCorrectiveAction,

    BigDecimal criticalMin,
    BigDecimal criticalMax,
    String unit,
    String monitoredDescription,

    @PositiveOrZero(message = "Interval start must be zero or positive")
    Long intervalStart,

    @Positive(message = "Interval repeat time must be positive")
    Long intervalRepeatTime
) {}
