package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CRCourseAssignmentResponse {

    private UUID courseAssignmentId;

    private CRCourseAssignment.CRAssignmentStatus status;

    private UserResponse user;

    public static CRCourseAssignmentResponse of(
            CRCourseAssignment crCourseAssignment) {
        return new CRCourseAssignmentResponse(
                crCourseAssignment.getCrCourseAssignmentId(),
                crCourseAssignment.getStatus(),
                UserResponse.of(crCourseAssignment.getCr())
        );


    }
}
