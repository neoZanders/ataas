package com.chalmers.atas.domain.crcourseassignment;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "cr_course_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CRCourseAssignment implements Serializable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID crCourseAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cr_user_id", nullable = false)
    private User cr;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseAssignmentStatus status;

    public static CRCourseAssignment of(User cr, Course course, CourseAssignmentStatus status) {
        CRCourseAssignment crCourseAssignment = new CRCourseAssignment();
        crCourseAssignment.cr = cr;
        crCourseAssignment.course = course;
        crCourseAssignment.status = status;
        return crCourseAssignment;
    }
}
