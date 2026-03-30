package com.chalmers.atas.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class AlgorithmTimeInterval {
    private LocalDateTime start;
    private LocalDateTime end;

    public boolean isOverlappingWith(AlgorithmTimeInterval other) {
        return this.start.isBefore(other.getEnd()) && other.getStart().isBefore(this.end);
    }

    public int getDurationMinutes() {
        return (int) Duration.between(start, end).toMinutes();
    }
}
