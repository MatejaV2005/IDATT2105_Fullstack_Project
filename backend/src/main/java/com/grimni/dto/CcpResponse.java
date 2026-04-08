package com.grimni.dto;

import java.math.BigDecimal;
import java.util.List;

public record CcpResponse(
    Long id,
    String name,
    String how,
    String equipment,
    String instructionsAndCalibration,
    String immediateCorrectiveAction,
    BigDecimal criticalMin,
    BigDecimal criticalMax,
    String unit,
    String monitoredDescription,
    CcpIntervalResponse interval,
    String repeatText,
    List<CcpUserResponse> verifiers,
    List<CcpUserResponse> deviationReceivers,
    List<CcpUserResponse> performers,
    List<CcpUserResponse> deputy,
    List<CcpCorrectiveMeasureResponse> ccpCorrectiveMeasures
) {}
