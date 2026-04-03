package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTAAssignmentRequest {
    private Integer minHours;
    private Integer maxHours;
    private CourseSessionType sessionTypePreference1;
    private CourseSessionType sessionTypePreference2;
    private CourseSessionType sessionTypePreference3;
    private CourseSessionType sessionTypePreference4;
    private Boolean isCompactSchedule;
}
