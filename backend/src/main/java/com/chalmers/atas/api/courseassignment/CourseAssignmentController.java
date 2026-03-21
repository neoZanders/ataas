package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.domain.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/course-assignments")
public class CourseAssignmentController {

    private final CourseAssignmentApplicationService courseAssignmentApplicationService;

    @PostMapping("/join")
    public HttpResponse<Void> joinCourse(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.joinCourse(courseId, currentUser));
    }

    @PostMapping("/cr-invitations")
    public HttpResponse<Void> inviteCR(
            @PathVariable UUID courseId,
            @RequestBody InviteCRRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.inviteCR(courseId, request, currentUser));
    }

    @GetMapping
    public HttpResponse<CourseAssignmentsResponse> getAssignments(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.getAssignments(courseId, currentUser));
    }

    @DeleteMapping("/crs/{crId}")
    public HttpResponse<Void> deleteCRAssignment(
            @PathVariable UUID courseId,
            @PathVariable UUID crId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.deleteCRAssignment(courseId, crId, currentUser));
    }
}
