package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTAAssignmentRequest {
    private Integer minHours;
    private Integer maxHours;
    private CourseSessionType sessionTypePreference;
    private Boolean isCompactSchedule;
}
