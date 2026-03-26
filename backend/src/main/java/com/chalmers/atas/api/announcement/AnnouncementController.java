package com.chalmers.atas.api.announcement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.domain.user.CurrentUser;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/announcements")
public class AnnouncementController {

    private final AnnouncementApplicationService announcementApplicationService;

    @GetMapping
    public HttpResponse<List<AnnouncementResponse>> getAnnouncements(
        @PathVariable UUID courseId,
        CurrentUser currentUser) {
        return HttpResponse.fromResult(announcementApplicationService.getAnnouncements(courseId, currentUser));
    }

    @PostMapping
    public HttpResponse<AnnouncementResponse> createAnnouncement(
        @PathVariable UUID courseId,
        @RequestBody CreateAnnouncementRequest request,
        CurrentUser currentUser) {
        return HttpResponse.fromResult(announcementApplicationService.createAnnouncement(courseId, request, currentUser));
    }
    
}
