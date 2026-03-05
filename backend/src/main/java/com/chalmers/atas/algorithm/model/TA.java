package com.chalmers.atas.algorithm.model;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TA {

    private final String id;    //Identity
    private final String name;  //Identity
    private final Set<String> availableSlots;    //Availability     "Monday15", "Tuesday12" etc
    private final int maxHoursPerLp;        //Workload limit
    private final Set<String> preferredDayTime;        //Preferred day
    private int totalAssignedHours;         //Current state


    //Constructor
    public TA(String id, String name, Set<String> availableSlots, Set<String> preferredDayTime, int maxHoursPerLp){
        this.id = id;
        this.name = name;
        this.availableSlots = availableSlots;
        this.preferredDayTime = preferredDayTime;
        this.maxHoursPerLp = maxHoursPerLp;
        this.totalAssignedHours = 0;
    }

    //check availability
    public boolean isAvailableAt(String timeSlot){
        return availableSlots.contains(timeSlot);
    }

    // --check preference 
    public boolean isPreferred(String timeSlot) {
        return preferredDayTime.contains(timeSlot);
    }

    //method add up the total time.
    public void addAssignedHours(int hours){
        this.totalAssignedHours += hours;
    }


    //getter
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public Set<String> getAvailableSlots(){return Collections.unmodifiableSet(availableSlots);} //read-only
    public Set<String> getPreferredDay (){return Collections.unmodifiableSet(preferredDayTime);} //read-only
    public int getMaxHoursPerLp(){
        return maxHoursPerLp;
    }
    public int getTotalAssignedHours(){return totalAssignedHours;}

}
