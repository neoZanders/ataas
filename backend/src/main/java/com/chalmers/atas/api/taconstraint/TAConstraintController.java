package com.chalmers.atas.api.taconstraint;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.domain.user.CurrentUser;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/ta-constraints")
public class TAConstraintController {

    private final TAConstraintApplicationService taConstraintApplicationService;

    @GetMapping
    public HttpResponse<List<TAConstraintResponse>> getCourseConstraints(
        @PathVariable UUID courseId,
        CurrentUser currentUser) {
        return HttpResponse.fromResult(taConstraintApplicationService.getCourseConstraints(courseId, currentUser));
    }

    @GetMapping("/{taId}")
    public HttpResponse<List<TAConstraintResponse>> getTAConstraints(
        @PathVariable UUID courseId,
        @PathVariable UUID taId,
        CurrentUser currentUser) {
        return HttpResponse.fromResult(taConstraintApplicationService.getTAConstraints(courseId, taId, currentUser));
    }

    @PostMapping
    public HttpResponse<Void> createTAConstraint(
        @PathVariable UUID courseId,
        @RequestBody CreateTAConstraintRequest request,
        CurrentUser currentUser) {
        return HttpResponse.fromResult(taConstraintApplicationService.createTAConstraint(courseId, request, currentUser));
    }

    @PatchMapping("/{taCourseSessionConstraintId}")
    public HttpResponse<TAConstraintResponse> updateTAConstraint(
        @PathVariable UUID courseId,
        @PathVariable UUID taCourseSessionConstraintId,
        @RequestBody UpdateTAConstraintRequest request,
        CurrentUser currentUser) {
            return HttpResponse.fromResult(taConstraintApplicationService.updateTAConstraint(courseId, taCourseSessionConstraintId, request, currentUser));
    }

    @DeleteMapping("/{taCourseSessionConstraintId}")
    public HttpResponse<Void> deleteTAConstraint(
        @PathVariable UUID courseId,
        @PathVariable UUID taCourseSessionConstraintId,
        CurrentUser currentUser) {
            return HttpResponse.fromResult(taConstraintApplicationService.deleteTAConstraint(courseId, taCourseSessionConstraintId, currentUser));
    }
}
