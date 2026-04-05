package com.chalmers.atas.algorithm.model;

import com.chalmers.atas.domain.coursesession.CourseSession;

import java.time.LocalDateTime;
import java.util.*;

public class Sessions {
    private final UUID sessionId;
    private final Timeslots timeslot;
    private final CourseSession.CourseSessionType sessionType;
    private final int minTa;
    private final int maxTA;
    private LocalDateTime courseStart;
    private LocalDateTime courseEnd;
    private final boolean isWeeklyRecurring;



    public Sessions(UUID sessionId, Timeslots timeslot, CourseSession.CourseSessionType sessionType, int minTa, int maxTa, LocalDateTime courseStart, LocalDateTime courseEnd, boolean isWeeklyRecurring){
        this.sessionId = sessionId;
        this.timeslot = timeslot;
        this.sessionType = sessionType;
        this.courseStart = courseStart;
        this.courseEnd = courseEnd;
        this.minTa = minTa;
        this.maxTA = maxTa;
        this.isWeeklyRecurring = isWeeklyRecurring;
    }

    //recurring
    public List<Sessions> recurring() {
        if(!isWeeklyRecurring) return List.of(this);

        List<Sessions> newSession = new ArrayList<>();
        Timeslots current = this.timeslot;


        while(!current.getStart().isAfter(courseEnd)) {
            newSession.add(new Sessions(UUID.randomUUID(), current,sessionType,minTa,maxTA,courseStart,courseEnd,false));
            current = current.addWeek(1);
        }
        return newSession;
    }



    //getter
    public UUID getSessionId(){return sessionId;}

    public Timeslots getTimeslot() {
        return timeslot;
    }

    public int getMinTa(){return minTa;}

    public int getMaxTA(){return maxTA;}



    public int getDurationTime(){return timeslot.getDurationHours();}

    public CourseSession.CourseSessionType getSessionType(){return sessionType;}

}
