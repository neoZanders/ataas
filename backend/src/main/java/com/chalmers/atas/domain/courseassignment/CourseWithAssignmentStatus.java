package com.chalmers.atas.domain.courseassignment;

import com.chalmers.atas.domain.course.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseWithAssignmentStatus {
    private final Course course;
    private final CourseAssignmentStatus status;
}
