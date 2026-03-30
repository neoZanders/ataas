package com.chalmers.atas.algorithmtest.choco;

import com.chalmers.atas.algorithm.model.AlgorithmResult;
import com.chalmers.atas.common.Result;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChocoAlgorithmTest extends ChocoAlgorithmTestBase {

    @Test
    public void testAllocate__mockScenario__ok() {
        Result<AlgorithmResult> naiveResult = chocoNaiveAlgorithmService.runAlgorithm(request);
        Result<AlgorithmResult> randomResult = chocoRandomLNSAlgorithmService.runAlgorithm(request);
        Result<AlgorithmResult> customResult = chocoCustomLNSAlgorithmService.runAlgorithm(request);

        assertTrue(naiveResult.isSuccess());
        assertTrue(randomResult.isSuccess());
        assertTrue(customResult.isSuccess());
        assertTrue(naiveResult.getData().feasible());
        assertTrue(randomResult.getData().feasible());
        assertTrue(customResult.getData().feasible());

        Map<Integer, String> modelByPenalty = new HashMap<>();
        modelByPenalty.put(naiveResult.getData().totalPenalty(), "Naive " + modelByPenalty.getOrDefault(naiveResult.getData().totalPenalty(), ""));
        modelByPenalty.put(randomResult.getData().totalPenalty(), "Random " + modelByPenalty.getOrDefault(randomResult.getData().totalPenalty(), ""));
        modelByPenalty.put(customResult.getData().totalPenalty(), "Custom " + modelByPenalty.getOrDefault(customResult.getData().totalPenalty(), ""));

        System.out.println("Best penalty: " + modelByPenalty.get(modelByPenalty.keySet().stream().min(Integer::compareTo).get()));
    }
}
