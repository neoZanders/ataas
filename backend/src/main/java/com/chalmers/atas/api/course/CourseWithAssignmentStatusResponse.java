package com.chalmers.atas.api.course;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseWithAssignmentStatusResponse {
    private CourseResponse course;
    private CourseAssignmentStatus assignmentStatus;

    public static CourseWithAssignmentStatusResponse of(Course course, CourseAssignmentStatus status) {
        return new CourseWithAssignmentStatusResponse(CourseResponse.of(course), status);
    }
}
