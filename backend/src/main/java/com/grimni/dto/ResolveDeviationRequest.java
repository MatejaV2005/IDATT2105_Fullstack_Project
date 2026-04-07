package com.grimni.dto;

import com.grimni.domain.enums.DeviationCategory;
import com.grimni.domain.enums.ReviewStatus;

public record ResolveDeviationRequest(
    String preventativeMeasureActuallyTaken
) {
}
