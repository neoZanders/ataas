package com.chalmers.atas.algorithm.modelCpGd;

import com.chalmers.atas.algorithm.modelCpGd.AlgorithmResult;

import java.util.List;

public interface AlgorithmService {
    AlgorithmType getType();
    AlgorithmResult run(List<TA> tas, List<Sessions> sessions);
}
