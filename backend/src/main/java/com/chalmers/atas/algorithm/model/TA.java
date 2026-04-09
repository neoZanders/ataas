package com.chalmers.atas.algorithm.model;
import com.chalmers.atas.domain.coursesession.CourseSession;


import java.util.List;
import java.util.UUID;

public class TA {

    private final UUID taID;
    private final int maxHoursPerLp;        //Workload limit
    private final int minHoursPerLp;
    List<CourseSession.CourseSessionType> sessionTypesPreferences;
    private final Boolean isCompactSchedule;
    private List<TAConstraint> constraints;
    private int totalAssignedHours;         //Curr ent state

    //Constructor
    public TA(UUID taID,
              int maxHoursPerLp,
              int minHoursPerLp,
              boolean isCompactSchedule,
              List<TAConstraint> constrains,
              List<CourseSession.CourseSessionType> sessionTypesPreferences){

        this.taID = taID;
        this.maxHoursPerLp = maxHoursPerLp;
        this.minHoursPerLp = minHoursPerLp;
        this.isCompactSchedule = isCompactSchedule;
        this.constraints = constrains;
        this.sessionTypesPreferences = sessionTypesPreferences;
        this.totalAssignedHours = 0;
    }


    //check availability
    public boolean isAvailableAt(Timeslots sessionSlot){
        return constraints.stream().anyMatch(constraint -> constraint.timeslots().overLapsWith(sessionSlot));
    }


    //
    public boolean hasCapacityToWork(int hours){
        return (hours + totalAssignedHours)<= maxHoursPerLp;
    }
    // --check preference 
    public boolean isPreferred(CourseSession.CourseSessionType type) {
        return  sessionTypesPreferences.contains(type);
    }

    //method add up the total time.
    public void addAssignedHours(int hours){
        this.totalAssignedHours += hours;
    }

    public boolean preferredSessionType(CourseSession.CourseSessionType type) {
        return sessionTypesPreferences.contains(type);
    }

    //getter
    public UUID getTaID(){return taID;}
    public int getMaxHoursPerLp(){return maxHoursPerLp;}
    public int getMinHoursPerLp(){return minHoursPerLp;}
    public List<CourseSession.CourseSessionType> getSessionType(){return sessionTypesPreferences;}
    public List<TAConstraint> getConstraints(){return constraints;}
    public int getTotalAssignedHours (){return totalAssignedHours;}
}
