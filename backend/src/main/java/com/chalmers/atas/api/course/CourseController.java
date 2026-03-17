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
    public HttpResponse<List<CourseResponse>> getCourses(
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.getCourses(currentUser));
    }

    @PutMapping("/{courseId}")
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

    @PostMapping("/{courseId}/courseConstraints")
    public HttpResponse<CourseConstraintResponse> createCourseConstraint(
            @PathVariable UUID courseId,
            @RequestBody CreateCourseRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.createCourseConstraint(courseId, request, currentUser));
    }

    @DeleteMapping("/{courseId}/courseConstraints/{courseConstraintId}")
    public HttpResponse<Void> deleteCourseConstraint(
            @PathVariable UUID courseId,
            @PathVariable UUID courseConstraintId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.deleteCourseConstraint(
                courseId,
                courseConstraintId,
                currentUser));
    }

    @GetMapping("/{courseId}/courseConstraints")
    public HttpResponse<List<CourseConstraintResponse>> getCourseConstraints(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.getCourseConstraints(courseId, currentUser));
    }

    @PostMapping("/{courseId}/courseSessions")
    public HttpResponse<CourseResponse> createCourseSession(
            @PathVariable UUID courseId,
            @RequestBody CreateCourseRequest request,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.createCourseSession(courseId, request, currentUser));
    }

    @DeleteMapping("/{courseId}/courseSessions/{courseSession}")
    public HttpResponse<Void> deleteCourseSession(
            @PathVariable UUID courseId,
            @PathVariable UUID courseSession,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.deleteCourseSession(
                courseId,
                courseSession,
                currentUser));
    }

    @GetMapping("/{courseId}/courseSessions")
    public HttpResponse<List<CourseResponse>> getCourseSession(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(courseApplicationService.getCourseSession(courseId, currentUser));
    }
}
