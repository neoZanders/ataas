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

    @PostMapping("/ta-invitations")
    public HttpResponse<Void> inviteTA(
            @PathVariable UUID courseId,
            @RequestBody InviteTARequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.inviteTA(courseId, request, currentUser));
    }

    @GetMapping
    public HttpResponse<CourseAssignmentsResponse> getAssignments(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.getAssignments(courseId, currentUser));
    }

    @PatchMapping("/tas/{taId}")
    public HttpResponse<TACourseAssignmentResponse> updateTAAssignment(
            @PathVariable UUID courseId,
            @PathVariable UUID taId, 
            CurrentUser currentUser,
            @RequestBody UpdateTAAssignmentRequest request) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.updateTAAssignment(courseId, taId, currentUser, request));
    }

    @DeleteMapping("/crs/{crId}")
    public HttpResponse<Void> deleteCRAssignment(
            @PathVariable UUID courseId,
            @PathVariable UUID crId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.deleteCRAssignment(courseId, crId, currentUser));
    }

    @DeleteMapping("/tas/{taId}")
    public HttpResponse<Void> deleteTAAssignment(
            @PathVariable UUID courseId,
            @PathVariable UUID taId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseAssignmentApplicationService.deleteTAAssignment(courseId, taId, currentUser));
    }
}
