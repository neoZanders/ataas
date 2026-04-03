package com.chalmers.atas.api.courseassignment;

import java.util.UUID;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TACourseAssignmentResponse {

    private UUID taCourseAssignmentId;

    private UserResponse ta;

    private CourseAssignmentStatus status;

    private Integer minHours;

    private Integer maxHours;

    private CourseSessionType sessionTypePreference1;
    private CourseSessionType sessionTypePreference2;
    private CourseSessionType sessionTypePreference3;
    private CourseSessionType sessionTypePreference4;

    private Boolean isCompactSchedule;

    public static TACourseAssignmentResponse of(TACourseAssignment taCourseAssignment){
        return new TACourseAssignmentResponse(
            taCourseAssignment.getTaCourseAssignmentId(), 
            UserResponse.of(taCourseAssignment.getTa()), 
            taCourseAssignment.getStatus(),
            taCourseAssignment.getMinHours(),
            taCourseAssignment.getMaxHours(),
            taCourseAssignment.getSessionTypePreference1(),
            taCourseAssignment.getSessionTypePreference2(),
            taCourseAssignment.getSessionTypePreference3(),
            taCourseAssignment.getSessionTypePreference4(),
            taCourseAssignment.getIsCompactSchedule());

    }

}
