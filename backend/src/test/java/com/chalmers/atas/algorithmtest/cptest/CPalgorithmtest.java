package com.chalmers.atas.algorithmtest.cptest;

import com.chalmers.atas.AlgorithmTestBase;
import com.chalmers.atas.algorithm.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CPalgorithmtest extends AlgorithmTestBase {
    private final CPscheduler cp = new CPscheduler();

    @Test
    @DisplayName("Feasible schedule")
    void feasibleTest() {
        AlgorithmResult result = cp.run(tas, sessions);
        assertTrue(result.isFeasible());
    }

    @Test
    @DisplayName("Min Tas on the sessions")
    void minTaSessionsTest() {
        AlgorithmResult result = cp.run(tas, sessions);
        for (ScheduleResult cpresult : result.getAllocations()) {
            int assigned = cpresult.getAssignedTaIds().size();
            int min = cpresult.getSession().getMinTa();
            assertTrue(assigned >= min, "Sessions not satisfied with amount of TA");
        }
    }


    @Test
    @DisplayName("No breaking Hardconstraint")
    void hardConstraintTests() {
        AlgorithmResult result = cp.run(tas, sessions);
        for (ScheduleResult cpresult : result.getAllocations()) {
            Timeslots timeslots = cpresult.getSession().getTimeslot();

            for (UUID taID : cpresult.getAssignedTaIds()) {
                TA ta = tas.stream().filter(t -> t.getTaID().equals(taID)).findFirst().orElseThrow();

                for (TAConstraint constraint : ta.getConstraints()) {
                    assertFalse(constraint.timeslots().overLapsWith(timeslots), " " + taID + "got assigned on hard constraint");
                }
            }
        }

    }

    @Test
    @DisplayName("doublebook test")
    void noTAShouldBeDoubleBooked() {
        AlgorithmResult result = cp.run(tas, sessions);
        List<ScheduleResult> allocations = result.getAllocations();

        for (int i = 0; i < allocations.size(); i++) {
            for (int j = i + 1; j < allocations.size(); j++) {
                ScheduleResult a = allocations.get(i);
                ScheduleResult b = allocations.get(j);

                boolean overlap = a.getSession().getTimeslot().overLapsWith(b.getSession().getTimeslot());
                if (!overlap) continue;

                for (UUID taId : a.getAssignedTaIds()) {
                    assertFalse(b.getAssignedTaIds().contains(taId),
                            "TA " + taId + " is double-booked");
                }
            }
        }
    }

    @Test
    @DisplayName("ta maximum workinghours test")
    void noTAShouldExceedMaxHours() {
        AlgorithmResult result = cp.run(tas, sessions);

        for (TA ta : tas) {
            int totalHours = result.getAllocations().stream()
                    .filter(sr -> sr.getAssignedTaIds().contains(ta.getTaID()))
                    .mapToInt(sr -> sr.getSession().getDurationTime())
                    .sum();

            assertTrue(totalHours <= ta.getMaxHoursPerLp(),
                    "TA " + ta.getTaID() + " exceeded max hours: " + totalHours + " > " + ta.getMaxHoursPerLp());
        }
    }

}