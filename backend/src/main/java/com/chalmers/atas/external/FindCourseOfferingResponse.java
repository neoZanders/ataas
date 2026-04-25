package com.chalmers.atas.external;

import java.util.List;

public record FindCourseOfferingResponse(
        List<CourseOfferingResponse> results,
        Long limit,
        Long page,
        Long totalPages,
        Long totalResults
) {}
