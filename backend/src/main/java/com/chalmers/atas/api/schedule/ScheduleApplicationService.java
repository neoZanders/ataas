package com.chalmers.atas.api.schedule;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.schedule.AlgorithmService;
import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.schedule.ScheduleService;
import com.chalmers.atas.domain.schedule.ScheduleSessionAllocationService;
import com.chalmers.atas.domain.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {

    private final CourseService courseService;
    private final AlgorithmService algorithmService;
    private final ScheduleService scheduleService;
    private final ScheduleSessionAllocationService scheduleSessionAllocationService;
    // TODO:
    // private final UserService userService;
    // private final CourseSessionService CourseSessionService;
    // private final TACourseAssignmentService TACourseAssignmentService;

    public Result<ScheduleResponse> createSchedule(
            CreateScheduleRequest request,
            UUID courseId,
            CurrentUser currentUser) {
        return scheduleService.createSchedule(courseId, currentUser.getUser())
                .flatMap(schedule -> scheduleSessionAllocationService
                        .getAllocations(courseId, currentUser.getUser())
                        .map(allocations -> ScheduleResponse.of(
                                schedule,
                                allocations.stream().map(ScheduleSessionAllocationResponse::of).toList()
                        )));
    }

    public Result<ScheduleResponse> getSchedule(UUID courseId, CurrentUser currentUser) {
        return scheduleService.getSchedule(courseId, currentUser.getUser())
                .flatMap(schedules -> {
                    if (schedules.isEmpty()) {
                        return Result.error(ErrorCode.NOT_FOUND.toError("Schedule not found"));
                    }

                    Schedule schedule = schedules.getFirst();
                    return scheduleSessionAllocationService
                            .getAllocations(courseId, currentUser.getUser())
                            .map(allocations -> ScheduleResponse.of(
                                    schedule,
                                    allocations.stream().map(ScheduleSessionAllocationResponse::of).toList()
                            ));
                });
    }
}
