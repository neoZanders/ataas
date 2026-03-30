package com.chalmers.atas.algorithmtest.choco;

import com.chalmers.atas.algorithm.choco.ChocoCustomLNSAlgorithmService;
import com.chalmers.atas.algorithm.choco.ChocoNaiveAlgorithmService;
import com.chalmers.atas.algorithm.choco.ChocoRandomLNSAlgorithmService;
import com.chalmers.atas.algorithmtest.AlgorithmTestBase;

public abstract class ChocoAlgorithmTestBase extends AlgorithmTestBase {
    protected final ChocoNaiveAlgorithmService chocoNaiveAlgorithmService = new ChocoNaiveAlgorithmService(600000, 10000, true);
    protected final ChocoRandomLNSAlgorithmService chocoRandomLNSAlgorithmService = new ChocoRandomLNSAlgorithmService(600000, 10000, true);
    protected final ChocoCustomLNSAlgorithmService chocoCustomLNSAlgorithmService = new ChocoCustomLNSAlgorithmService(600000, 10000, true);
}
