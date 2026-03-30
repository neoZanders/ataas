package com.chalmers.atas.algorithm.model;

import java.util.UUID;

public record AlgorithmHardSessionConstraint(
        UUID taAssignmentId,
        AlgorithmTimeInterval timeInterval
) {}
