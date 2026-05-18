package com.chalmers.atas.algorithm.modelCpGd;


import com.chalmers.atas.algorithm.AlgorithmService;
import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.common.Result;

import java.util.*;

public class SchedulerChannel implements AlgorithmService {
    private final CPScheduler cPscheduler = new CPScheduler();
    private final Greedy greedy = new Greedy();

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.HYBRID;
    }

    @Override
    public Result<AlgorithmResult> runAlgorithm(AlgorithmRequest request) {

        Result<AlgorithmResult> cpResultWrapper = cPscheduler.runAlgorithm(request);

        if (!cpResultWrapper.isSuccess()) {
            System.out.println("Failed CP (" + cpResultWrapper.getError().getMessage());
        }

        Map<UUID, List<UUID>> emptyAssignment = new HashMap<>();
        for (AlgorithmSession sr : request.sessions()) {
            UUID sessionId = sr.sessionId();
            ArrayList<UUID> emptyList = new ArrayList<>();
            emptyAssignment.put(sessionId, emptyList);
        }

        Map<UUID, Integer> emptyMinutes = new HashMap<>();
        for (AlgorithmTA ta : request.tas()) {
            UUID taID = ta.taAssignmentId();
            emptyMinutes.put(taID, 0);

        }

        AlgorithmResult cpResult = cpResultWrapper.getData();
        Map<UUID, List<UUID>> cpAssignment = new HashMap<>();
        for (AlgorithmSessionAllocation allocation : cpResult.allocations()) {
            cpAssignment.put(allocation.sessionId(), new ArrayList<>(allocation.taAssignmentIds()));
        }

        Map<UUID, Integer> assignedMinutes = new HashMap<>();
        request.tas().forEach(ta -> assignedMinutes.put(ta.taAssignmentId(), 0));

        for (AlgorithmSessionAllocation alloc : cpResult.allocations()) {
            AlgorithmSession session = request.sessions().stream()
                    .filter(s -> s.sessionId().equals(alloc.sessionId()))
                    .findFirst().orElseThrow();
            int durationMinutes = session.timeInterval().getDurationMinutes();
            for (UUID taId : alloc.taAssignmentIds()) {
                assignedMinutes.merge(taId, durationMinutes, Integer::sum);
            }
        }
        AlgorithmResult greedyResult = greedy.fillingTheRest(request, cpAssignment, assignedMinutes);
        return Result.ok(greedyResult);
    }
}
