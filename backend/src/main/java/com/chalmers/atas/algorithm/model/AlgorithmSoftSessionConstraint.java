package com.chalmers.atas.algorithm.model;

import java.util.UUID;

public record AlgorithmSoftSessionConstraint(
        UUID taAssignmentId,
        AlgorithmTimeInterval timeInterval,
        int weight
) {}
