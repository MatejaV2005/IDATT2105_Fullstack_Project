package com.grimni.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.grimni.domain.FileObject;
import com.grimni.domain.ProductCategory;

public record DangerAnalysisProductCategoryResponse(
    Long id,
    String productName,
    String productDescription,
    Long flowchartFileId,
    String flowchartFileName,
    String flowchartPreviewUrl,
    LocalDateTime createdAt,
    List<DangerRiskComboResponse> dangerRiskCombos
) {
    public static DangerAnalysisProductCategoryResponse fromEntity(ProductCategory category) {
        List<DangerRiskComboResponse> combos = category.getDangerRiskCombos().stream()
            .map(DangerRiskComboResponse::fromEntity)
            .toList();

        FileObject flowchartFile = category.getFlowchartFile();
        Long flowchartFileId = flowchartFile != null ? flowchartFile.getId() : null;
        String flowchartFileName = flowchartFile != null ? flowchartFile.getFileName() : null;
        // Preview URLs are generated client-side (or by a future signed-url endpoint); leave null here.
        String flowchartPreviewUrl = null;

        return new DangerAnalysisProductCategoryResponse(
            category.getId(),
            category.getProductName(),
            category.getProductDescription(),
            flowchartFileId,
            flowchartFileName,
            flowchartPreviewUrl,
            category.getCreatedAt(),
            combos
        );
    }
}
