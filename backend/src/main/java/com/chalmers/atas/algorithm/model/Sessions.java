package com.chalmers.atas.algorithm.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Sessions {
    private final String courseId;
    private final String sessionId;
    private final Set<String> timeSlots;
    private final int durationTime;
    private final int requiredAmountTa;


    public Sessions(String courseId, String sessionId, Set<String> timeSlots, int durationTime, int requiredAmountTa){
        this.courseId = courseId;
        this.sessionId = sessionId;
        this.timeSlots = Collections.unmodifiableSet(new HashSet<>(timeSlots));
        this.durationTime = durationTime;
        this.requiredAmountTa = requiredAmountTa;
    }

    public String getCourseId(){return courseId;}
    public String getSessionId(){return sessionId;}

    public Set<String> getTimeSlots(){return Collections.unmodifiableSet(timeSlots);}

    public int getDurationTime(){return durationTime;}

    public int getRequiredAmountTa(){return requiredAmountTa;}

}
