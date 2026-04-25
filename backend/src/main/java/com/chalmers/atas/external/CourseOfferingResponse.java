package com.chalmers.atas.external;

import java.util.List;

public record CourseOfferingResponse(
        Integer id,
        List<FieldResponse> fields
) {}
