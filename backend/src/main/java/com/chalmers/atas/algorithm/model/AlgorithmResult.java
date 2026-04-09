package com.chalmers.atas.algorithm.model;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AlgorithmResult {
    private final List<ScheduleResult> allocations;
    private final boolean feasible;
    private final List<UUID> unfeasibleSessionIds;

    private AlgorithmResult(List<ScheduleResult> allocations, boolean feasible, List<UUID> unfeasibleSessionIds){
        this.allocations = allocations;
        this.feasible = feasible;
        this.unfeasibleSessionIds = unfeasibleSessionIds;
    }

    public static AlgorithmResult fesiable(List<ScheduleResult> allocations) {
        return new AlgorithmResult(allocations,true,List.of());
    }

    public static AlgorithmResult infeasible(List<ScheduleResult> allocations){
        List<UUID> unfeasible = allocations.stream().filter(allocation -> allocation.isFullyStaffed())
                .map(allocation -> allocation.getSession().getSessionId())
                .collect(Collectors.toUnmodifiableList());
        return new AlgorithmResult(allocations, false, unfeasible);
    }

    //getter
    public List<ScheduleResult> getAllocations() {return allocations;}
    public boolean isFeasible() {return feasible;}
    public List<UUID> getUnfeasibleSessionIds() {
        return unfeasibleSessionIds;
    }
}
