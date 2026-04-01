package com.chalmers.atas.domain.tacourseassignment;

import java.io.Serializable;
import java.util.UUID;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import com.chalmers.atas.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ta_course_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TACourseAssignment implements Serializable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID taCourseAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ta_user_id", nullable = false)
    private User ta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseAssignmentStatus status;

    @Column
    private Integer minHours;
    
    @Column
    private Integer maxHours;

    @Enumerated(EnumType.STRING)
    @Column
    private CourseSessionType sessionTypePreference;

    @Column
    private Boolean isCompactSchedule;

    public static TACourseAssignment of(
            User ta,
            Course course,
            CourseAssignmentStatus status,
            Integer minHours,
            Integer maxHours,
            CourseSessionType sessionTypePreference,
            Boolean isCompactSchedule
        ){
        TACourseAssignment taCourseAssignment = new TACourseAssignment();
        taCourseAssignment.ta = ta;
        taCourseAssignment.course = course;
        taCourseAssignment.status = status;
        taCourseAssignment.minHours = minHours;
        taCourseAssignment.maxHours = maxHours;
        taCourseAssignment.sessionTypePreference = sessionTypePreference;
        taCourseAssignment.isCompactSchedule = isCompactSchedule;
        return taCourseAssignment;
    }


}
