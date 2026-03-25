package com.chalmers.atas.domain.coursesession;

import com.chalmers.atas.domain.course.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSession implements Serializable {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID courseSessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseSessionType courseSessionType;

    @Column(name = "min_tas", nullable = false)
    private int minTAs;

    @Column(name = "max_tas", nullable = false)
    private int maxTAs;

    @Column
    private boolean isWeeklyRecurring;

    public enum CourseSessionType {
        GRADING,
        LABORATION,
        HELP,
        EXERCISE
    }

    public static CourseSession of(
            Course course,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            CourseSessionType courseSessionType,
            int minTAs,
            int maxTAs,
            boolean isWeeklyRecurring) {
        CourseSession courseSession = new CourseSession();
        courseSession.course = course;
        courseSession.startDateTime = startDateTime;
        courseSession.endDateTime = endDateTime;
        courseSession.courseSessionType = courseSessionType;
        courseSession.minTAs = minTAs;
        courseSession.maxTAs = maxTAs;
        courseSession.isWeeklyRecurring = isWeeklyRecurring;
        return courseSession;
    }
}
