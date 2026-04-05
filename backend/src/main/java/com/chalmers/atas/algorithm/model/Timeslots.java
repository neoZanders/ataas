package com.chalmers.atas.algorithm.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Timeslots {
    private final LocalDateTime start;
    private final LocalDateTime end;


    public Timeslots(LocalDateTime start, LocalDateTime end){
        this.start = start;
        this.end = end;
    }


    public boolean overLapsWith(Timeslots other){
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }


    public boolean contains(Timeslots other){
        return !this.start.isAfter(other.start) && !this.end.isBefore(other.end);
    }

    //for recurring
    public Timeslots addWeek(int week){
        return new Timeslots(start.plusWeeks(week), end.plusWeeks(week));
    }

    public int getDurationHours(){
        return (int) Duration.between(start,end).toHours();
    }

    public LocalDateTime getStart(){return start;}

    public LocalDateTime getEnd(){return end;}
}

