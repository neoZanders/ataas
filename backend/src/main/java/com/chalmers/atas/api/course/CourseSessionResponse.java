package com.chalmers.atas.api.course;

import com.chalmers.atas.domain.coursesession.CourseSession;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseSessionResponse {

    private UUID courseSessionId;

    private UUID courseId;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    @NotNull
    private CourseSession.CourseSessionType courseSessionType;
    @NotNull
    private int minTAs;
    @NotNull
    private int maxTAs;

    @JsonProperty("isWeeklyRecurring")
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
