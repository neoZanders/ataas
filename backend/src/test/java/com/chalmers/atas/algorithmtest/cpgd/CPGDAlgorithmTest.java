package com.chalmers.atas.algorithmtest.cpgd;

import com.chalmers.atas.algorithm.model.AlgorithmRequest;
import com.chalmers.atas.algorithm.model.AlgorithmResult;
import com.chalmers.atas.algorithmtest.AlgorithmRequestGenerator;
import com.chalmers.atas.common.Result;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CPGDAlgorithmTest extends CPGDAlgorithmTestBase {

    @Test
    public void testAllocate__mockScenario__ok() {

        System.out.println("Scenario: mock_scenario");
        runAndAssertAllAlgorithms("mock_scenario",mockRequest);
    }


    @Test
    public void testAllocate__smallGeneratedScenario__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "small_generated",
                        1L,
                        LocalDate.of(2026, 1, 19),
                        2,
                        6,
                        6,
                        1,
                        3,
                        1,
                        2,
                        100
                )
        );


        System.out.println("Scenario: small_generated");
        runAndAssertAllAlgorithms("small_generated",request);
    }

    @Test
    public void testAllocate__mediumGeneratedScenario__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "medium_generated",
                        2L,
                        LocalDate.of(2026, 1, 19),
                        5,
                        8,
                        8,
                        1,
                        4,
                        2,
                        4,
                        100
                )
        );


        System.out.println("Scenario: medium_generated");
        runAndAssertAllAlgorithms("medium_generate",request);
    }

    @Test
    public void testAllocate__manySoftConstraints__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "many_soft_constraints",
                        3L,
                        LocalDate.of(2026, 1, 19),
                        4,
                        8,
                        8,
                        1,
                        4,
                        1,
                        10,
                        100
                )
        );


        System.out.println("Scenario: many_soft_constraints");
        runAndAssertAllAlgorithms("many_soft_constraints",request);
    }

    @Test
    public void testAllocate__manyHardConstraints__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "many_hard_constraints",
                        4L,
                        LocalDate.of(2026, 1, 19),
                        4,
                        10,
                        8,
                        1,
                        4,
                        4,
                        3,
                        100
                )
        );


        System.out.println("Scenario: many_hard_constraints");
        runAndAssertAllAlgorithms("many_hard_constraints",request);
    }

    @Test
    public void testAllocate__higherStaffingRequirement__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "higher_staffing_requirement",
                        5L,
                        LocalDate.of(2026, 1, 19),
                        3,
                        10,
                        7,
                        2,
                        5,
                        2,
                        4,
                        100
                )
        );

        System.out.println("Scenario: higher_staffing_requirement");
        runAndAssertAllAlgorithms("higher_staffing_requirement",request);
    }

    @Test
    public void testAllocate__denseSessionsFewTAs__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "dense_sessions_few_tas",
                        6L,
                        LocalDate.of(2026, 1, 19),
                        6,
                        4,
                        8,
                        1,
                        2,
                        2,
                        6,
                        100
                )
        );

        System.out.println("Scenario: dense_sessions_few_tas");
        runAndAssertAllAlgorithms("dense_sessions_few_tas", request);
    }

    @Test
    public void testAllocate__sparseSessionsManyTAs__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "sparse_sessions_many_tas",
                        7L,
                        LocalDate.of(2026, 1, 19),
                        4,
                        12,
                        5,
                        1,
                        3,
                        2,
                        8,
                        100
                )
        );

        System.out.println("Scenario: sparse_sessions_many_tas");
        runAndAssertAllAlgorithms("sparse_sessions_many_tas", request);
    }

    @Test
    public void testAllocate__sessionPreferenceConflict__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "session_preference_conflict",
                        9L,
                        LocalDate.of(2026, 1, 19),
                        5,
                        8,
                        9,
                        1,
                        4,
                        1,
                        6,
                        100
                )
        );

        System.out.println("Scenario: session_preference_conflict");
        runAndAssertAllAlgorithms("session_preference_conflict", request);
    }

    @Test
    public void testAllocate__largeGeneratedScenario__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "large_generated",
                        10L,
                        LocalDate.of(2026, 1, 19),
                        8,
                        12,
                        12,
                        1,
                        4,
                        3,
                        8,
                        100
                )
        );

        System.out.println("Scenario: large_generated");
        runAndAssertAllAlgorithms("large_generated", request);
    }

    @Test
    public void testAllocate__veryManySoftConstraints__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "very_many_soft_constraints",
                        11L,
                        LocalDate.of(2026, 1, 19),
                        5,
                        10,
                        10,
                        1,
                        4,
                        1,
                        20,
                        100
                )
        );

        System.out.println("Scenario: very_many_soft_constraints");
        runAndAssertAllAlgorithms("very_many_soft_constraints", request);
    }

    @Test
    public void testAllocate__tightHourBudgets__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "tight_hour_budgets",
                        12L,
                        LocalDate.of(2026, 1, 19),
                        4,
                        7,
                        8,
                        1,
                        3,
                        2,
                        5,
                        100
                )
        );

        System.out.println("Scenario: tight_hour_budgets");
        runAndAssertAllAlgorithms("tight_hour_budgets", request);
    }

    @Test
    public void testAllocate__baselineNoSoftConstraints__ok() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "baseline_no_soft_constraints",
                        13L,
                        LocalDate.of(2026, 1, 19),
                        4,
                        8,
                        8,
                        1,
                        4,
                        2,
                        0,
                        100
                )
        );

        System.out.println("Scenario: baseline_no_soft_constraints");
        runAndAssertAllAlgorithms("baseline_no_soft_constraints", request);
    }

    private void runAndAssertAllAlgorithms(String scenarioName, AlgorithmRequest request) {

        Result<AlgorithmResult> cpSchedulerResult = cpScheduler.runAlgorithm(request);
        Result<AlgorithmResult> greedyResult = greedy.runAlgorithm(request);

        long channelStart = System.nanoTime();
        Result<AlgorithmResult> schedulerChannelResult = schedulerChannel.runAlgorithm(request);
        long channelTime = (System.nanoTime() - channelStart) / 1_000_000; // convert to ms

        System.out.println("SchedulerChannel: time=" + channelTime + "ms");

        assertAlgorithmResultOk(cpSchedulerResult);
        assertAlgorithmResultOk(greedyResult);
        assertAlgorithmResultOk(schedulerChannelResult);

        printBestPenalty(cpSchedulerResult, greedyResult, schedulerChannelResult);
    }

    private void assertAlgorithmResultOk(Result<AlgorithmResult> result) {
        System.out.println("  success=" + result.isSuccess()
                + " feasible=" + (result.isSuccess() ? result.getData().feasible() : "n/a"));
        assertTrue(result.isSuccess());
        assertTrue(result.getData().feasible());
    }

    private void printBestPenalty(
            Result<AlgorithmResult> cpSchedulerResult,
            Result<AlgorithmResult> greedyResult,
            Result<AlgorithmResult> schedulerChannelResult
    ) {
        System.out.println("CPScheduler penalty:      " + cpSchedulerResult.getData().totalPenalty());
        System.out.println("Greedy penalty:           " + greedyResult.getData().totalPenalty());
        System.out.println("SchedulerChannel penalty: " + schedulerChannelResult.getData().totalPenalty());

        Map<Integer, String> modelByPenalty = new HashMap<>();

        modelByPenalty.put(
                cpSchedulerResult.getData().totalPenalty(),
                "CPScheduler " + modelByPenalty.getOrDefault(cpSchedulerResult.getData().totalPenalty(), "")
        );

        modelByPenalty.put(
                greedyResult.getData().totalPenalty(),
                "Greedy " + modelByPenalty.getOrDefault(greedyResult.getData().totalPenalty(), "")
        );

        modelByPenalty.put(
                schedulerChannelResult.getData().totalPenalty(),
                "SchedulerChannel " + modelByPenalty.getOrDefault(schedulerChannelResult.getData().totalPenalty(), "")
        );

        Integer bestPenalty = modelByPenalty.keySet()
                .stream()
                .min(Integer::compareTo)
                .get();

        System.out.println("best_penalty=" + bestPenalty + ", models: " + modelByPenalty.get(bestPenalty));
    }
}
