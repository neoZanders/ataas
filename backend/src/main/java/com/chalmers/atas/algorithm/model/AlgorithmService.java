package com.chalmers.atas.algorithm.model;

import java.util.List;

public interface AlgorithmService {
    AlgorithmType getType();
    AlgorithmResult run(List<TA> tas, List<Sessions> sessions);
}
