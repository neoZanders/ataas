package com.chalmers.atas.api.course;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.course.Course;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseConstraintResponse {

    private UUID constraintId;

    private String constraint;

    private UserResponse cr;

    private Course.CourseStatus status;

    public static CourseConstraintResponse of(CourseConstraint cc) {
        return new CourseConstraintResponse(
                cc.getConstraintId(),
                cc.getConstraint(),
                cc.getCourse().getCourseId()
        );
    }
}
