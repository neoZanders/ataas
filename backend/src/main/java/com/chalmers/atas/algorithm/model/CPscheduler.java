package com.chalmers.atas.algorithm.model;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverSolutionCallback;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CPscheduler {
    static {
        Loader.loadNativeLibraries();
    }

    public Map<String ,List<String> solve(List<TA> tas, List<Sessions> sessions){
        CpModel model = new CpModel();

        int numTas = tas.size();
        int numSessions = sessions.size();


        //creates variables
        Literal[][] shifts = new Literal[numTas][numSessions];
        for(int i = 0; i < numTas; i++){
            for(int y = 0; y < numSessions; y++ ){
                shifts[i][y] = model.newBoolVar("shifts" + i + "_" + y);
            }
        }


        //timeMatching constraint availability.
        for(int i = 0; i < numTas; i++){
            for(int y = 0; y < numSessions; y++){
                TA ta = tas.get(i);
                Sessions session = sessions.get(y);

                boolean avaiable =  session.getTimeSlots().stream().anyMatch(ta::isAvailableAt);

                //if not avaiable, then pair is not assigned = 0
                if(!avaiable){
                    model.addEquality(shifts[i][y], 0);
                }
            }
        }

        //maximum working hours constraint
        for(int i = 0; i < numTas; i++){
            TA ta = tas.get(i);

        }




    }
}
