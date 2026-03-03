package com.chalmers.atas.api.course;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.course.Course;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseResponse {

    private UUID courseId;

    private String courseCode;

    private UserResponse cr;

    private Course.CourseStatus status;

    public static CourseResponse of(Course course) {
        return new CourseResponse(
                course.getCourseId(),
                course.getCourseCode(),
                UserResponse.of(course.getCr()),
                course.getCourseStatus()
        );
    }
}
