package com.grimni.dto;

import java.time.LocalDateTime;

import com.grimni.domain.DangerRiskCombo;

public record DangerRiskComboResponse(
    Long id,
    String danger,
    String dangerCorrectiveMeasure,
    Integer severityScore,
    Integer likelihoodScore,
    LocalDateTime createdAt
) {
    public static DangerRiskComboResponse fromEntity(DangerRiskCombo combo) {
        return new DangerRiskComboResponse(
            combo.getId(),
            combo.getDanger(),
            combo.getDangerCorrectiveMeasure(),
            combo.getSeverityScore(),
            combo.getLikelihoodScore(),
            combo.getCreatedAt()
        );
    }
}
