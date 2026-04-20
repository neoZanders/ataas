package com.chalmers.atas.api.schedule;

import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.domain.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class ScheduleController {

    private final ScheduleApplicationService scheduleApplicationService;

    @GetMapping("/{courseId}/schedule")
    public HttpResponse<ScheduleResponse> getSchedule(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(scheduleApplicationService.getSchedule(courseId, currentUser));
    }

    @PostMapping("/{courseId}/schedule")
    public HttpResponse<ScheduleResponse> createSchedule(
            @PathVariable UUID courseId,
            CurrentUser currentUser) {
        return HttpResponse.fromResult(scheduleApplicationService.createSchedule(courseId, currentUser));
    }
}
