package com.chalmers.atas.api.courseassignment;

import java.util.UUID;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment.TAAssignmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TACourseAssignmentResponse {

    private UUID taCourseAssignmentId;

    private UserResponse ta;

    private TAAssignmentStatus status;

    private int minHours;

    private int maxHours;

    private CourseSessionType sessionTypePreference;

    private Boolean isCompactSchedule;

    public static TACourseAssignmentResponse of(TACourseAssignment taCourseAssignment){
        return new TACourseAssignmentResponse(
            taCourseAssignment.getTaCourseAssignmentId(), 
            UserResponse.of(taCourseAssignment.getTa()), 
            taCourseAssignment.getStatus(),
            taCourseAssignment.getMinHours(),
            taCourseAssignment.getMaxHours(),
            taCourseAssignment.getSessionTypePreference(), 
            taCourseAssignment.getIsCompactSchedule());

    }

}
