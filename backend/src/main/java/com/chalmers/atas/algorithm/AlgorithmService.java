package com.chalmers.atas.algorithm;

import com.chalmers.atas.algorithm.model.AlgorithmRequest;
import com.chalmers.atas.algorithm.model.AlgorithmResult;
import com.chalmers.atas.common.Result;

public interface AlgorithmService {
    AlgorithmType getType();
    Result<AlgorithmResult> runAlgorithm(AlgorithmRequest request);
}
