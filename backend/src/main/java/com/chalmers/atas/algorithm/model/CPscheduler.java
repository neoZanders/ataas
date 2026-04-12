package com.chalmers.atas.algorithm.model;

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
    public AlgorithmResult run(List<TA> tas, List<Sessions> sessions){

        //recurring into week instance
        List<Sessions> moreSessions = sessions.stream().flatMap(session -> session.recurring().stream())
                .collect(Collectors.toList());


        CpModel model = new CpModel();
        int numTas = tas.size();
        int numSessions = moreSessions.size();


        //creates variables
        IntVar[][] shifts = new IntVar[numTas][numSessions];
        for(int i = 0; i < numTas; i++){
            for(int j = 0; j < numSessions; j++ ){
                shifts[i][j] = model.newBoolVar("shifts" + i + "_" + j);
            }
        }

        //timeMatching constraint availability.
        for(int i = 0; i < numTas; i++){
            for(int y = 0; y < numSessions; y++){
                if(!tas.get(i).isAvailableAt(moreSessions.get(y).getTimeslot())){
                    model.addEquality(shifts[i][y], 0);
                }
            }
        }

        //min max per TA
        for(int i = 0; i < numTas; i++){
            TA ta = tas.get(i);
            IntVar[] taVar = new IntVar[numSessions];
            long[] durations = new long [numSessions];
            for(int j = 0; j< numSessions; j++){
                taVar[j] = shifts[i][j];
                durations[j] = moreSessions.get(j).getDurationTime();
            }
            model.addLinearConstraint(LinearExpr.weightedSum(taVar, durations), ta.getMinHoursPerLp(), ta.getMaxHoursPerLp()
            );
        }


        //min max per Session
        for(int i = 0; i < numSessions; i++){
            Sessions session = moreSessions.get(i);
            IntVar[] sessionVar = new IntVar[numTas];
            for(int j = 0; j < numTas; j++){
                sessionVar[j] = shifts[j][i];
            }
            model.addLinearConstraint(LinearExpr.sum(sessionVar), session.getMinTa(), session.getMaxTA()
            );
        }

        //for doublebooking
        for(int i = 0; i < numTas; i++){
            for(int j = 0; j < numSessions; j++){
                for(int k = j + 1;k < numSessions; k++ ){
                    if(moreSessions.get(j).getTimeslot().overLapsWith(moreSessions.get(k).getTimeslot())){
                        model.addLinearConstraint(LinearExpr.sum(new IntVar[]{shifts[i][j], shifts[i][k]}), 0,1);
                    }
                }
            }
        }

        //solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);


        Map<UUID, List<UUID>> assignmentMap = new HashMap<>();
        moreSessions.forEach(s -> assignmentMap.put(s.getSessionId(), new ArrayList<>()));

        if(status == CpSolverStatus.FEASIBLE || status == CpSolverStatus.OPTIMAL){
            for(int i = 0; i < numTas; i++){
                for(int j = 0; j < numSessions; j++){
                if(solver.value(shifts[i][j]) == 1){
                    UUID sessionsID = moreSessions.get(j).getSessionId();
                    assignmentMap.get(sessionsID).add(tas.get(i).getTaID());
                    tas.get(i).addAssignedHours(moreSessions.get(j).getDurationTime());
                }
            }
        }
            System.out.println("Cp solver status: " + status);
        }else {
            System.out.println("Warning: CP Solver status: " + status);
        }
        List<ScheduleResult> results = moreSessions.stream()
                .map(session -> new ScheduleResult(session, assignmentMap.get(session.getSessionId())))
                .collect(Collectors.toList());

        boolean allStaffed = results.stream().allMatch(ScheduleResult::isFullyStaffed);
        if(allStaffed)
            return AlgorithmResult.fesiable(results);
        else
            return AlgorithmResult.infeasible(results);
    }

}
