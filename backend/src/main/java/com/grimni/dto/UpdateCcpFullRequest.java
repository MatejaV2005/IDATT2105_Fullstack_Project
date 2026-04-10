package com.grimni.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCcpFullRequest(
    @NotNull Long id,
    @Size(min = 1) String name,
    @Size(min = 1) String how,
    @Size(min = 1) String equipment,
    @Size(min = 1) String instructionsAndCalibration,
    @Size(min = 1) String immediateCorrectiveAction,
    BigDecimal criticalMin,
    BigDecimal criticalMax,
    String unit,
    String monitoredDescription,
    List<Long> verifiers,
    List<Long> deviationRecievers,
    List<Long> performers,
    List<Long> deputy,
    @Valid List<CreateCcpCorrectiveMeasureRequest> ccpCorrectiveMeasure
) {}
