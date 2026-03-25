package com.chalmers.atas.api.course;

import com.chalmers.atas.domain.coursesession.CourseSession;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseSessionResponse {

    private UUID courseSessionId;

    private UUID courseId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private CourseSession.CourseSessionType courseSessionType;

    private int minTAs;

    private int maxTAs;

    private boolean isWeeklyRecurring;

    public static CourseSessionResponse of(CourseSession courseSession) {
        return new CourseSessionResponse(
                courseSession.getCourseSessionId(),
                courseSession.getCourse().getCourseId(),
                courseSession.getStartDateTime(),
                courseSession.getEndDateTime(),
                courseSession.getCourseSessionType(),
                courseSession.getMinTAs(),
                courseSession.getMaxTAs(),
                courseSession.isWeeklyRecurring()
        );
    }
}
