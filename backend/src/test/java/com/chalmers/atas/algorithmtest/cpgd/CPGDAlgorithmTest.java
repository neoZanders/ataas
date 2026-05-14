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
        runAndAssertAllAlgorithms(mockRequest);
    }

    @Test
    public void testAllocate__forcedPenalty__penaltyIsNonZero() {
        AlgorithmRequest request = AlgorithmRequestGenerator.generate(
                new AlgorithmRequestGenerator.Config(
                        "forced_penalty",
                        6L,
                        LocalDate.of(2026, 1, 19),
                        2,
                        2,
                        4,
                        1,
                        1,
                        0,
                        2,
                        100
                )
        );

        System.out.println("Scenario: forced_penalty");
        runAndAssertAllAlgorithms(request);

        Result<AlgorithmResult> greedyResult = greedy.runAlgorithm(request);
        Result<AlgorithmResult> channelResult = schedulerChannel.runAlgorithm(request);
        Result<AlgorithmResult> cpResult = cpScheduler.runAlgorithm(request);

        assertTrue(
                greedyResult.getData().totalPenalty() > 0,
                "Expected non-zero penalty from Greedy but got 0"
        );
        assertTrue(
                channelResult.getData().totalPenalty() > 0,
                "Expected non-zero penalty from SchedulerChannel but got 0"
        );
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
        runAndAssertAllAlgorithms(request);
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
        runAndAssertAllAlgorithms(request);
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
        runAndAssertAllAlgorithms(request);
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
        runAndAssertAllAlgorithms(request);
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
        runAndAssertAllAlgorithms(request);
    }

    private void runAndAssertAllAlgorithms(AlgorithmRequest request) {


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
