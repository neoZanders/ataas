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
}
