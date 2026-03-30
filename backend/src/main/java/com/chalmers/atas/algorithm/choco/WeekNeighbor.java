package com.chalmers.atas.algorithm.choco;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.lns.neighbors.IntNeighbor;
import org.chocosolver.solver.variables.IntVar;

public class WeekNeighbor extends IntNeighbor {

    private final int taCount;
    private final int sessionCount;

    private final int[] sessionWeekIndex;
    private final int weekCount;

    private final int initialRelaxedWeekCount;
    private final int relaxedWeekStep;

    private int startWeek = 0;
    private int relaxedWeekCount;

    public WeekNeighbor(
            IntVar[] variables,
            int taCount,
            int sessionCount,
            int[] sessionWeekIndex,
            int weekCount,
            int initialRelaxedWeekCount,
            int relaxedWeekStep
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
        if (sessionWeekIndex.length != sessionCount) {
            throw new IllegalArgumentException(
                    "sessionWeekIndex.length must equal sessionCount"
            );
        }
        if (weekCount <= 0) {
            throw new IllegalArgumentException("weekCount must be > 0");
        }
        if (initialRelaxedWeekCount <= 0 || initialRelaxedWeekCount > weekCount) {
            throw new IllegalArgumentException(
                    "initialRelaxedWeekCount must be in [1, weekCount]"
            );
        }
        if (relaxedWeekStep <= 0) {
            throw new IllegalArgumentException("relaxedWeekStep must be > 0");
        }

        this.taCount = taCount;
        this.sessionCount = sessionCount;
        this.sessionWeekIndex = sessionWeekIndex;
        this.weekCount = weekCount;
        this.initialRelaxedWeekCount = initialRelaxedWeekCount;
        this.relaxedWeekStep = relaxedWeekStep;
        this.relaxedWeekCount = initialRelaxedWeekCount;
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        for (int s = 0; s < sessionCount; s++) {
            if (isRelaxedWeek(sessionWeekIndex[s])) {
                continue;
            }

            for (int t = 0; t < taCount; t++) {
                freeze(t * sessionCount + s);
            }
        }

        startWeek = (startWeek + 1) % weekCount;
    }

    @Override
    public void restrictLess() {
        if (relaxedWeekCount < weekCount) {
            relaxedWeekCount = Math.min(weekCount, relaxedWeekCount + relaxedWeekStep);
        }
    }

    @Override
    public boolean isSearchComplete() {
        return relaxedWeekCount >= weekCount;
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        resetRelaxation();
    }

    @Override
    public void loadFromSolution(Solution solution) {
        super.loadFromSolution(solution);
        resetRelaxation();
    }

    private void resetRelaxation() {
        this.relaxedWeekCount = initialRelaxedWeekCount;
        this.startWeek = 0;
    }

    private boolean isRelaxedWeek(int weekIndex) {
        int distance = (weekIndex - startWeek + weekCount) % weekCount;
        return distance < relaxedWeekCount;
    }
}
