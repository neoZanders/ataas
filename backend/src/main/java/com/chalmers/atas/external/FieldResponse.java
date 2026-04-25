package com.chalmers.atas.external;

import java.util.List;

public record FieldResponse (
        String fieldId,
        List<String> values
) {}
