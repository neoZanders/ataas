package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseAssignmentsResponse {

    private UUID courseId;

    private List<CRCourseAssignmentResponse> crCourseAssignments;

    private List<TACourseAssignmentResponse> taCourseAssignments;

    public static CourseAssignmentsResponse of(
            UUID courseId,
            List<CRCourseAssignment> crCourseAssignments,
            List<TACourseAssignment> taCourseAssignment
    ) {
        return new CourseAssignmentsResponse(
                courseId,
                crCourseAssignments.stream().map(CRCourseAssignmentResponse::of).toList(),
                taCourseAssignment.stream().map(TACourseAssignmentResponse::of).toList()
        );
    }
}
