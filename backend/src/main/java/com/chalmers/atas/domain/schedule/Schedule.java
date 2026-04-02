package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.domain.course.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule implements Serializable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public static Schedule of(String name, Course course) {
        Schedule schedule = new Schedule();
        schedule.course = course;
        return schedule;
    }
}
