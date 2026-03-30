package com.chalmers.atas.algorithm.model;

import java.util.List;
import java.util.UUID;

public record AlgorithmSessionAllocation(
        UUID sessionId,
        List<UUID> taAssignmentIds
) {}
