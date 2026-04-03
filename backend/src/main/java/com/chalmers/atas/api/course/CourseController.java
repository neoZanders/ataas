package com.chalmers.atas.api.course;

import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.domain.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseApplicationService courseApplicationService;

    @PostMapping
    public HttpResponse<CourseResponse> createCourse(
            @RequestBody CreateCourseRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.createCourse(request, currentUser));
    }

    @GetMapping
    public HttpResponse<List<CourseWithAssignmentStatusResponse>> getCourses(
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.getCourses(currentUser));
    }

    @PutMapping("/{courseId}/archive")
    public HttpResponse<CourseResponse> archiveCourse(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.archiveCourse(courseId, currentUser));
    }

    @DeleteMapping("/{courseId}")
    public HttpResponse<Void> deleteCourse(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.deleteCourse(courseId, currentUser));
    }

    @PatchMapping("/{courseId}")
    public HttpResponse<CourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @RequestBody UpdateCourseRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.updateCourse(courseId, request, currentUser));
    }

    @GetMapping("/{courseId}/course-sessions")
    public HttpResponse<List<CourseSessionResponse>> getCourseSessions(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.getCourseSessions(courseId, currentUser));
    }

    @PostMapping("/{courseId}/course-sessions")
    public HttpResponse<CourseSessionResponse> createCourseSession(
            @PathVariable UUID courseId,
            @RequestBody CreateCourseSessionRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.createCourseSession(courseId, request, currentUser));
    }

    @DeleteMapping("/{courseId}/course-sessions/{courseSessionId}")
    public HttpResponse<Void> deleteCourseSession(
            @PathVariable UUID courseId,
            @PathVariable UUID courseSessionId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.deleteCourseSession(courseId, courseSessionId, currentUser));
    }
}
