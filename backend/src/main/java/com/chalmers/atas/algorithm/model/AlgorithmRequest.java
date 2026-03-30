package com.chalmers.atas.algorithm.model;

import java.util.List;

public record AlgorithmRequest(
        List<AlgorithmSession> sessions,
        List<AlgorithmTA> tas,
        List<AlgorithmHardSessionConstraint> hardConstraints,
        List<AlgorithmSoftSessionConstraint> softConstraints
) {}
