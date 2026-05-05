package com.chalmers.atas.algorithm.modelCpGd;

import com.chalmers.atas.algorithm.modelCpGd.AlgorithmResult;

import java.util.*;
import java.util.stream.Collectors;

public class SchedulerChannel implements AlgorithmService {
    private final CPscheduler cPscheduler = new CPscheduler();
    private final Greedy greedy = new Greedy();

    @Override
    public AlgorithmType getType(){
        return AlgorithmType.HYBRID;
    }
    @Override
    public AlgorithmResult run(List<TA> tas, List<Sessions> sessions){
        AlgorithmResult cpResult = cPscheduler.run(tas, sessions);


        Map<UUID, List<UUID>> cpAssignment = new HashMap<>();
        for(ScheduleResult sr : cpResult.getAllocations()){
            cpAssignment.put(sr.getSession().getSessionId(), new ArrayList<>(sr.getAssignedTaIds()));
        }

        List<Sessions> moreSessions = cpResult.getAllocations().stream()
                .map(ScheduleResult::getSession)
                .collect(Collectors.toList());

        return greedy.fillingTheRest(tas, moreSessions,cpAssignment);
    }

}
