package com.chalmers.atas.algorithm.model;


import java.util.Set;

public class TA {

    private final String id;    //Identity
    private final String name;  //Identity
    private final Set<String> availableSlots;    //Availability     "Monday15", "Tuesday12" etc
    private final int maxHoursPerLp;        //Workload limit
    private final Set<String> preferredDay;        //Preferred day
    private int totalAssignedHours;         //Current state


    //Constructor
    public TA(String id, String name, Set<String> availableSlots, Set<String> preferredDay, int maxHoursPerLp){
        this.id = id;
        this.name = name;
        this.availableSlots = availableSlots;
        this.preferredDay = preferredDay;
        this.maxHoursPerLp = maxHoursPerLp;
        this.totalAssignedHours = 0;
    }

    //check availability
    public boolean isAvailableAt(String timeSlots){
        return availableSlots.contains(timeSlots);
    }

    // --check preference 
    public boolean isPreferred(String timeSlots) {
        return true;
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
    public Set<String> getAvailableSlots(){return availableSlots;}
    public Set<String> getPreferredDay (){return preferredDay;}
    public int getMaxHoursPerLp(){
        return maxHoursPerLp;
    }
    public int getTotalAssignedHours(){return totalAssignedHours;}

}
