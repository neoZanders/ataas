package com.chalmers.atas.api.course;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.course.Course;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseSessionResponse {

    private UUID courseId;

    private String name;

    private UUID sessionId;

    public static CourseSessionResponse of(CourseSession session) { // CourseSession not implemented
        return new CourseSessionResponse(
                session.getSessionId(),
                session.getName(),
                session.getCourse().getCourseId()
        );
    }
}
