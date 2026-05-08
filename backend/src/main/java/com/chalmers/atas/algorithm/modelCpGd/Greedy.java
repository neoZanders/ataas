
package com.chalmers.atas.algorithm.modelCpGd;

import com.chalmers.atas.algorithm.AlgorithmService;
import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.common.Result;

import java.util.*;
import java.util.stream.Collectors;

public class Greedy implements AlgorithmService {

    @Override
    public AlgorithmType getType() {return AlgorithmType.GREEDY;}

    @Override
    public Result<AlgorithmResult> runAlgorithm(AlgorithmRequest request) {


        Map<UUID, List<UUID>> assignments = new HashMap<>();
        request.sessions().forEach(s -> assignments.put(s.sessionId(), new ArrayList<>()));

        Map<UUID, Integer> assignedMinute = new HashMap<>();
        request.tas().forEach(ta -> assignedMinute.put(ta.taAssignmentId(),0));

        AlgorithmResult result = fillingTheRest(request,assignments, assignedMinute);

        return Result.ok(result);
    }

    public AlgorithmResult fillingTheRest(AlgorithmRequest request, Map<UUID, List<UUID>> assignment, Map<UUID, Integer> assignedMinutes){

        List<AlgorithmSession> sessions           = request.sessions();
        List<AlgorithmTA> tas                     = request.tas();
        List<AlgorithmHardSessionConstraint> hard = request.hardConstraints();
        List<AlgorithmSoftSessionConstraint> soft = request.softConstraints();


        sessions.forEach(s -> assignment.putIfAbsent(s.sessionId(), new ArrayList<>()));
        tas.forEach(ta -> assignedMinutes.putIfAbsent(ta.taAssignmentId(),0));
        //timematching
        Map<UUID, List<AlgorithmTA>> candidates = step1_timeMatching(tas, sessions,hard,assignment,assignedMinutes);

        //timegrouping
        List<List<AlgorithmSession>> groups = step2_timeGrouping(sessions);
        boolean allOrginized = false;
        while(!allOrginized) {
            boolean madeProgress = false;
            groups.sort((g1,g2) -> Integer.compare(g2.size(),g1.size()));
            for(List<AlgorithmSession> group : groups){
                group.sort((s1, s2) -> {
                    int r1 = s1.minTAs() - assignment.get(s1.sessionId()).size();
                    int r2 = s2.maxTAs() - assignment.get(s2.sessionId()).size();
                    return Integer.compare(r2,r1);
                });

                for(AlgorithmSession session : group) {
                    if(isFullyStaffed(session, assignment) || assignment.get(session.sessionId()).size() >=
                    session.maxTAs()) continue;

                    List<AlgorithmTA> eligable = candidates.getOrDefault(session.sessionId(), List.of());
                    if(eligable.isEmpty()) continue;

                    eligable.sort(Comparator.comparingInt((AlgorithmTA ta) -> softPenalty(ta, session,soft))
                            .thenComparingInt(ta -> constraintScore(ta, sessions,hard, assignment,assignedMinutes))
                            .thenComparingInt(ta -> ta.sessionTypePreferences().indexOf(session.type()))
                            .thenComparingInt(ta -> assignedMinutes.get(ta.taAssignmentId()))) ;

                    for(AlgorithmTA ta : eligable){
                        if(isFullyStaffed(session,assignment ) ||
                                assignment.get(session.sessionId()).size() >= session.maxTAs()) break;
                        if(assignment.get(session.sessionId()).contains(ta.taAssignmentId())) continue;
                        if(!hasCapacityToWork(ta, session,assignedMinutes)) continue;


                        assignment.get(session.sessionId()).add(ta.taAssignmentId());
                        assignedMinutes.merge(ta.taAssignmentId(), session.timeInterval().getDurationMinutes(), Integer::sum);
                        madeProgress = true;
                        candidates = step1_timeMatching(tas, sessions, hard, assignment, assignedMinutes);
                    }
                    //testing
                    if(!isFullyStaffed(session,assignment)){
                        System.out.println("Warning: not fully staff" + session.sessionId() + " "
                                + assignment.get(session.sessionId()).size() + session.minTAs());
                    }
                }
            }
            boolean allDone = sessions.stream().allMatch(s -> isFullyStaffed(s, assignment));
            if (allDone || !madeProgress) {
                allOrginized = true;
            }

        }
        List<AlgorithmSessionAllocation> allocations = sessions.stream()
                .map(s -> new AlgorithmSessionAllocation(s.sessionId(), assignment.getOrDefault(s.sessionId(), new ArrayList<>())))
                .collect(Collectors.toList());

        boolean feasible = allocations.stream().allMatch(
                a -> {AlgorithmSession s = sessions.stream()
                        .filter(x -> x.sessionId().equals(a.sessionId()))
                        .findFirst().orElseThrow();
                return a.taAssignmentIds().size() >= s.minTAs();
                });

        int totalPenaty = computeTotalPenalty(allocations,sessions,tas, soft);
        return new AlgorithmResult(allocations,totalPenaty,feasible,false);


    }//end of filling

    private Map<UUID, List<AlgorithmTA>> step1_timeMatching(List<AlgorithmTA> tas, List<AlgorithmSession> sessions,
                                                               List<AlgorithmHardSessionConstraint> hardConstraints,
                                                               Map<UUID, List<UUID>> assignment,
                                                               Map<UUID, Integer> assignedMinutes
    ){
        Map<UUID, List<AlgorithmTA>> candidates = new HashMap<>();

        for(AlgorithmSession sessions1 : sessions){
            if(assignment.get(sessions1.sessionId()).size() >= sessions1.maxTAs()) continue;
            List<AlgorithmTA> eligable = tas.stream().filter(ta -> isAvailable(ta,sessions1,hardConstraints))
                    .filter(ta -> hasCapacityToWork(ta, sessions1,assignedMinutes))
                    .filter(ta -> !assignment.get(sessions1.sessionId()).contains(ta.taAssignmentId()))
                    .collect(Collectors.toList());
            candidates.put(sessions1.sessionId(), eligable);

        }
        return candidates;
    }



    private List<List<AlgorithmSession>> step2_timeGrouping(List<AlgorithmSession> sessions){
        List<List<AlgorithmSession>> groups = new ArrayList<>();
        boolean[] visited = new boolean[sessions.size()];

        for (int i = 0; i < sessions.size(); i++) {
            if (visited[i]) continue;
            List<AlgorithmSession> group = new ArrayList<>();
            group.add(sessions.get(i));
            visited[i] = true;

            for(int j = 0; j < sessions.size(); j++) {
                if (visited[j]) continue;
                if (sessions.get(i).timeInterval().isOverlappingWith(sessions.get(j).timeInterval())) {
                    group.add(sessions.get(j));
                    visited[j] = true;
                }
            }
            groups.add(group);
        }
        return groups;
    }

    private boolean hasCapacityToWork(AlgorithmTA ta, AlgorithmSession session, Map<UUID, Integer> assignedMinutes){
        int used = assignedMinutes.getOrDefault(ta.taAssignmentId(), 0);
        int sessionMinutes = session.timeInterval().getDurationMinutes();
        // maxHours is stored as hours — convert to minutes for comparison
        return (used + sessionMinutes) <= ta.maxHours() * 60;
    }



    private boolean isAvailable(
            AlgorithmTA ta,
            AlgorithmSession session,
            List<AlgorithmHardSessionConstraint> hardConstraints
    ) {
        return hardConstraints.stream()
                .filter(c -> c.taAssignmentId().equals(ta.taAssignmentId()))
                .noneMatch(c -> c.timeInterval().isOverlappingWith(session.timeInterval()));
    }


    private int constraintScore(AlgorithmTA ta, List<AlgorithmSession> sessions, List<AlgorithmHardSessionConstraint> hardConstraints, Map<UUID,List<UUID>> assignment,
                                            Map<UUID, Integer> assignedMinutes){

        return (int) sessions.stream().filter(s -> !isFullyStaffed(s, assignment))
                .filter(s -> assignment.get(s.sessionId()).size() < s.maxTAs())
                .filter(s-> isAvailable(ta, s, hardConstraints))
                .filter(s -> hasCapacityToWork(ta, s, assignedMinutes))
                .filter(s -> !assignment.get(s.sessionId()).contains(ta.taAssignmentId())).count();
    }


    private int softPenalty(
            AlgorithmTA ta,
            AlgorithmSession session,
            List<AlgorithmSoftSessionConstraint> softConstraints
    ) {
        return softConstraints.stream()
                .filter(c -> c.taAssignmentId().equals(ta.taAssignmentId()))
                .filter(c -> c.timeInterval().isOverlappingWith(session.timeInterval()))
                .mapToInt(AlgorithmSoftSessionConstraint::weight)
                .sum();
    }

    private int computeTotalPenalty(
            List<AlgorithmSessionAllocation> allocations,
            List<AlgorithmSession> sessions,
            List<AlgorithmTA> tas,
            List<AlgorithmSoftSessionConstraint> softConstraints
    ) {
        Map<UUID, AlgorithmTA> taById = new HashMap<>();
        tas.forEach(ta -> taById.put(ta.taAssignmentId(), ta));
        int total = 0;
        for (AlgorithmSessionAllocation alloc : allocations) {
            AlgorithmSession session = sessions.stream()
                    .filter(s -> s.sessionId().equals(alloc.sessionId()))
                    .findFirst().orElseThrow();
            for (UUID taId : alloc.taAssignmentIds()) {
                total += softConstraints.stream()
                        .filter(c -> c.taAssignmentId().equals(taId))
                        .filter(c -> c.timeInterval().isOverlappingWith(session.timeInterval()))
                        .mapToInt(AlgorithmSoftSessionConstraint::weight)
                        .sum();
            }
        }
        return total;
    }

    private boolean isFullyStaffed(AlgorithmSession sessions, Map<UUID, List<UUID>> assignment){
        return assignment.get(sessions.sessionId()).size() >= sessions.minTAs();
    }



}//end
