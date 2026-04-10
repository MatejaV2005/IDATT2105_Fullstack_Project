package com.grimni.dto;

public record OrgAnalysisResponse(
    PrerequisiteRoutineRecordStats prerequisiteRoutineRecord,
    CcpRecordStats ccpRecords,
    DeviationStats deviations,
    UserStats users,
    ResourceStats resources
) {
    public record PrerequisiteRoutineRecordStats(
        long completed,
        long failed
    ) {}

    public record CcpRecordStats(
        long skipped,
        long verified,
        long rejected,
        long waiting
    ) {}

    public record DeviationStats(
        DeviationCategoryStats ikMat,
        DeviationCategoryStats ikAlkohol,
        DeviationCategoryStats other
    ) {}

    public record DeviationCategoryStats(
        long open,
        long closed
    ) {}

    public record UserStats(
        long owners,
        long managers,
        long workers
    ) {}

    public record ResourceStats(
        long routines,
        long ccps,
        long productCategories
    ) {}
}
