package com.chalmers.atas.algorithm.modelCpGd;

import com.chalmers.atas.algorithm.AlgorithmService;
import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.common.Result;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.*;
import java.util.stream.Collectors;

public class CPscheduler implements AlgorithmService {

    static {
        Loader.loadNativeLibraries();
    }

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.CP_SAT;
    }

    @Override
    public Result<AlgorithmResult> runAlgorithm(AlgorithmRequest request){
        List<AlgorithmTA> tas          = request.tas();
        List<AlgorithmSession> sessions = request.sessions();
        List<AlgorithmHardSessionConstraint> hardConstraints = request.hardConstraints();


        int numTas = tas.size();
        int numSessions = sessions.size();

        CpModel model = new CpModel();

        //creates variables
        IntVar[][] shifts = new IntVar[numTas][numSessions];
        for(int i = 0; i < numTas; i++){
            for(int j = 0; j < numSessions; j++ ){
                shifts[i][j] = model.newBoolVar("shifts" + i + "_" + j);
            }
        }

        //timeMatching constraint availability.
        for(int i = 0; i < numTas; i++){
            AlgorithmTA ta = tas.get(i);
            final UUID taID = ta.taAssignmentId();
                List<AlgorithmTimeInterval> blocked = hardConstraints.stream()
                        .filter(c -> c.taAssignmentId().equals(ta.taAssignmentId()))
                        .map(AlgorithmHardSessionConstraint::timeInterval)
                        .collect(Collectors.toList());
            for(int j = 0; j < numSessions; j++){
                AlgorithmTimeInterval sessionInterval = sessions.get(j).timeInterval();
                boolean unavaiable = blocked.stream().anyMatch(b -> b.isOverlappingWith(sessionInterval));
                if(unavaiable){
                    model.addEquality(shifts[i][j], 0);
                }
            }
        }

        //min max per TA
        for(int i = 0; i < numTas; i++){
            AlgorithmTA ta = tas.get(i);
            IntVar[] taVar = new IntVar[numSessions];
            long[] durations = new long [numSessions];
            for(int j = 0; j< numSessions; j++){
                taVar[j] = shifts[i][j];
                durations[j] = sessions.get(j).timeInterval().getDurationMinutes();
            }
            model.addLinearConstraint(LinearExpr.weightedSum(shifts[i], durations), ta.minHours()*60L, ta.maxHours()*60L);
        }


        //min max per Session
        for(int i = 0; i < numSessions; i++){
            AlgorithmSession session = sessions.get(i);
            IntVar[] sessionVar = new IntVar[numTas];
            for(int j = 0; j < numTas; j++){
                sessionVar[j] = shifts[j][i];
            }
            model.addLinearConstraint(LinearExpr.sum(sessionVar), session.minTAs(), session.maxTAs()
            );
        }

        //for doublebooking
        for(int i = 0; i < numTas; i++){
            for(int j = 0; j < numSessions; j++){
                for(int k = j + 1;k < numSessions; k++ ){
                    if(sessions.get(j).timeInterval().isOverlappingWith(sessions.get(k).timeInterval())){
                        model.addLinearConstraint(LinearExpr.sum(new IntVar[]{shifts[i][j], shifts[i][k]}), 0,1);
                    }
                }
            }
        }

        //solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);


        Map<UUID, List<UUID>> assignmentMap = new HashMap<>();
        sessions.forEach(s -> assignmentMap.put(s.sessionId(), new ArrayList<>()));

        if(status == CpSolverStatus.FEASIBLE || status == CpSolverStatus.OPTIMAL){
            for(int i = 0; i < numTas; i++){
                for(int j = 0; j < numSessions; j++){
                if(solver.value(shifts[i][j]) == 1){
                    assignmentMap.get(sessions.get(j).sessionId()).add(tas.get(i).taAssignmentId());
                }
            }
        }
            System.out.println("Cp solver status: " + status);
        }else {
            System.out.println("Warning: CP Solver status: " + status);
        }
        List<AlgorithmSessionAllocation> allocations = sessions.stream()
                .map(session -> new AlgorithmSessionAllocation(session.sessionId(), assignmentMap.get(session.sessionId())))
                .collect(Collectors.toList());

        boolean feasible = allocations.stream().allMatch(
                a -> {AlgorithmSession s = sessions.stream().
                filter(x -> x.sessionId().equals(a.sessionId())).findFirst().orElseThrow();
                return a.taAssignmentIds().size() >= s.minTAs();
                });


        AlgorithmResult result = new AlgorithmResult(allocations, 0,feasible, status == CpSolverStatus.OPTIMAL);
        return Result.ok(result);
    }

}
