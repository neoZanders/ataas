package com.chalmers.atas.algorithm.choco;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.lns.neighbors.IntNeighbor;
import org.chocosolver.solver.variables.IntVar;


public class TANeighbor extends IntNeighbor {

    private final int taCount;
    private final int sessionCount;

    private final int initialRelaxedTACount;
    private final int relaxedTAStep;

    private int startTA = 0;
    private int relaxedTACount;

    public TANeighbor(
            IntVar[] variables,
            int taCount,
            int sessionCount,
            int initialRelaxedTACount,
            int relaxedTAStep
    ) {
        super(variables);

        if (taCount <= 0) {
            throw new IllegalArgumentException("taCount must be > 0");
        }
        if (sessionCount <= 0) {
            throw new IllegalArgumentException("sessionCount must be > 0");
        }
        if (variables.length != taCount * sessionCount) {
            throw new IllegalArgumentException(
                    "variables.length must equal taCount * sessionCount"
            );
        }
        if (initialRelaxedTACount <= 0 || initialRelaxedTACount > taCount) {
            throw new IllegalArgumentException(
                    "initialRelaxedTACount must be in [1, taCount]"
            );
        }
        if (relaxedTAStep <= 0) {
            throw new IllegalArgumentException("relaxedTAStep must be > 0");
        }

        this.taCount = taCount;
        this.sessionCount = sessionCount;
        this.initialRelaxedTACount = initialRelaxedTACount;
        this.relaxedTAStep = relaxedTAStep;
        this.relaxedTACount = initialRelaxedTACount;
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        for (int t = 0; t < taCount; t++) {
            if (isRelaxedTA(t)) {
                continue;
            }

            int rowStart = t * sessionCount;
            int rowEnd = rowStart + sessionCount;

            for (int i = rowStart; i < rowEnd; i++) {
                freeze(i);
            }
        }

        startTA = (startTA + 1) % taCount;
    }

    @Override
    public void restrictLess() {
        if (relaxedTACount < taCount) {
            relaxedTACount = Math.min(taCount, relaxedTACount + relaxedTAStep);
        }
    }

    @Override
    public boolean isSearchComplete() {
        return relaxedTACount >= taCount;
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        resetRelaxation();
    }

    @Override
    public void loadFromSolution(org.chocosolver.solver.Solution solution) {
        super.loadFromSolution(solution);
        resetRelaxation();
    }

    private void resetRelaxation() {
        this.relaxedTACount = initialRelaxedTACount;
        this.startTA = 0;
    }

    private boolean isRelaxedTA(int taIndex) {
        int distance = (taIndex - startTA + taCount) % taCount;
        return distance < relaxedTACount;
    }
}