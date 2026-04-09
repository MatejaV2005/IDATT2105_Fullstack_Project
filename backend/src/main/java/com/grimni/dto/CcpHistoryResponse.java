package com.grimni.dto;

import java.util.List;
import java.math.BigDecimal;

public record CcpHistoryResponse(
    Long id,
    String name,
    List<CcpRecordResponse> records
) {
    public record CcpRecordResponse(
        Long id,
        BigDecimal value,
        BigDecimal min,
        BigDecimal max,
        String unit,
        String comment,
        CollaboratorResponse performedBy
    ) { }
}