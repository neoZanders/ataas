package com.chalmers.atas.algorithm.model;

import java.util.List;

public record AlgorithmResult(
        List<AlgorithmSessionAllocation> allocations,
        int totalPenalty,
        boolean feasible,
        boolean provenOptimal
) {}
