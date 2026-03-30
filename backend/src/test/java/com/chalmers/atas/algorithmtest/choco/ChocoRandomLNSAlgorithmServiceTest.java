package com.chalmers.atas.algorithmtest.choco;

import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.common.Result;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType.*;
import static com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType.EXERCISE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChocoRandomLNSAlgorithmServiceTest extends ChocoAlgorithmTestBase {

    @Test
    public void testAllocate__singleTAToSingleSession__ok() {
        UUID taId = UUID.randomUUID();
        AlgorithmRequest request = new AlgorithmRequest(
                List.of(new AlgorithmSession(
                        UUID.randomUUID(),
                        new AlgorithmTimeInterval(
                                LocalDateTime.now(),
                                LocalDateTime.now().plusHours(2)),
                        LABORATION,
                        1,
                        2)),
                List.of(new AlgorithmTA(
                        taId,
                        0,
                        3,
                        List.of(LABORATION, GRADING, HELP, EXERCISE),
                        false)),
                List.of(),
                List.of()
        );

        Result<AlgorithmResult> result = chocoRandomLNSAlgorithmService.runAlgorithm(request);

        assertTrue(result.isSuccess());
        AlgorithmResult algorithmResult = result.getData();

        assertTrue(algorithmResult.feasible());
        assertEquals(0, algorithmResult.totalPenalty());
        assertEquals(1, algorithmResult.allocations().size());
        assertEquals(
                List.of(taId),
                algorithmResult.allocations().getFirst().taAssignmentIds()
        );
    }

    @Test
    public void testAllocate__mockScenario__ok() {
        Result<AlgorithmResult> result = chocoRandomLNSAlgorithmService.runAlgorithm(request);

        System.out.println(
                "totalPenalty=" + result.getData().totalPenalty() +
                        ", provenOptimal=" + result.getData().provenOptimal());
        assertTrue(result.isSuccess());
        assertTrue(result.getData().feasible());
    }
}
