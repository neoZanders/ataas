
package com.chalmers.atas.algorithm.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.cfg.SessionEventSettings;

import java.util.*;
import java.util.stream.Collectors;

public class Greedy implements AlgorithmService {

    @Override
    public AlgorithmType getType() {return AlgorithmType.GREEDY;}

    @Override
    public AlgorithmResult run(List<TA> tas, List<Sessions> sessions) {

        List<Sessions> moreSessions = sessions.stream().flatMap(sessions1 -> sessions1.recurring().stream())
                .collect(Collectors.toUnmodifiableList());

        Map<UUID, List<UUID>> assignments = new HashMap<>();
        moreSessions.forEach(s -> assignments.put(s.getSessionId(), new ArrayList<>()));
        return fillingTheRest(tas, moreSessions,assignments);
    }

    public AlgorithmResult fillingTheRest(List<TA> tas, List<Sessions> sessions, Map<UUID, List<UUID>> assignment){

        sessions.forEach(s -> assignment.putIfAbsent(s.getSessionId(), new ArrayList<>()));


        //timematching
        Map<UUID, List<TA>> candidates = step1_timeMatching(tas, sessions,assignment);

        //timegrouping
        List<List<Sessions>> groups = step2_timeGrouping(sessions);


        boolean allOrginized = false;
        while(!allOrginized) {
            groups.sort((g1,g2) -> Integer.compare(g2.size(),g1.size()));
            for(List<Sessions> group : groups){
                group.sort((s1, s2) -> {
                    int r1 = s1.getMinTa() - assignment.get(s1.getSessionId()).size();
                    int r2 = s2.getMinTa() - assignment.get(s2.getSessionId()).size();
                    return Integer.compare(r2,r1);
                });

                for(Sessions session : group) {
                    if(isFullyStaffed(session, assignment) || assignment.get(session.getSessionId()).size() >=
                    session.getMaxTA()) continue;

                    List<TA> eligable = candidates.getOrDefault(session.getSessionId(), List.of());
                    if(eligable.isEmpty()) continue;

                    eligable.sort(Comparator.comparingInt((TA ta) -> constrantScore(ta, sessions,assignment))
                            .thenComparingInt(ta -> ta.preferredSessionType(session.getSessionType()) ? 0 : 1)
                            .thenComparingInt(TA::getTotalAssignedHours)
                    ) ;

                    for(TA ta : eligable){
                        if(isFullyStaffed(session,assignment ) ||
                                assignment.get(session.getSessionId()).size() >= session.getMaxTA()) break;
                        if(assignment.get(session.getSessionId()).contains(ta.getTaID())) continue;
                        if(!ta.hasCapacityToWork(session.getDurationTime())) continue;


                        assignment.get(session.getSessionId()).add(ta.getTaID());
                        ta.addAssignedHours(session.getDurationTime());
                        candidates = step1_timeMatching(tas, sessions,assignment);
                    }
                    //testing
                    if(!isFullyStaffed(session,assignment)){
                        System.out.println("Warning: not fully staff" + session.getSessionId() + " "
                                + assignment.get(session.getSessionId()).size() + session.getMinTa());
                    }
                }
            }
            allOrginized = sessions.stream().allMatch(s -> isFullyStaffed(s, assignment));

            
        }
    }

    private Map<UUID, List<TA>> step1_timeMatching(List<TA> tas, List<Sessions> sessions, Map<UUID, List<UUID>> assignments){
        Map<UUID, List<TA>> candidates = new HashMap<>();

        for(Sessions sessions1 : sessions){
            if(assignments.get(sessions1.getSessionId()).size() >= sessions1.getMaxTA()) continue;
            List<TA> eligable = tas.stream().filter(ta -> ta.isAvailableAt(sessions1.getTimeslot()))
                    .filter(ta -> ta.hasCapacityToWork(sessions1.getDurationTime()))
                    .filter(ta -> !assignments.get(sessions1.getSessionId()).contains(ta.getTaID()))
                    .collect(Collectors.toUnmodifiableList());
            candidates.put(sessions1.getSessionId(), eligable);

        }
        return candidates;
    }



    private List<List<Sessions>> step2_timeGrouping(List<Sessions> sessions){
        List<List<Sessions>> groups = new ArrayList<>();
        boolean[] visited = new boolean[sessions.size()];

        for (int i = 0; i < sessions.size(); i++) {
            if (visited[i]) continue;
            List<Sessions> group = new ArrayList<>();
            group.add(sessions.get(i));
            visited[i] = true;
            for(int j = 0; j < sessions.size(); i++) {
                if (visited[j]) continue;
                if (sessions.get(i).getTimeslot().overLapsWith(sessions.get(j).getTimeslot())) {
                    group.add(sessions.get(j));
                    visited[j] = true;
                }
            }
            groups.add(group);
        }
        return groups;
    }

    private int constrantScore(TA ta, List<Sessions> sessions, Map<UUID,List<UUID>> assignment){
        return (int) sessions.stream().filter(s -> !isFullyStaffed(s, assignment))
                .filter(s -> assignment.get(s.getSessionId()).size() < s.getMaxTA())
                .filter(s-> ta.isAvailableAt(s.getTimeslot()))
                .filter(s -> ta.hasCapacityToWork(s.getDurationTime()))
                .filter(s -> !assignment.get(s.getSessionId()).contains(ta.getTaID())).count();
    }


    private boolean isFullyStaffed(Sessions sessions, Map<UUID, List<UUID>> assignment){
        return assignment.get(sessions.getSessionId()).size() >= sessions.getMaxTA();
    }




}//end
