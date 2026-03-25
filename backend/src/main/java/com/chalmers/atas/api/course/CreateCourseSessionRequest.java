package com.chalmers.atas.api.course;

import com.chalmers.atas.domain.coursesession.CourseSession;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateCourseSessionRequest {

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    private CourseSession.CourseSessionType courseSessionType;

    @NotNull
    private Integer minTAs;

    @NotNull
    private Integer maxTAs;

    @NotNull
    private Boolean isWeeklyRecurring;
}
