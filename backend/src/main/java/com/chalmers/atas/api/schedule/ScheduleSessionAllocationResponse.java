package com.chalmers.atas.api.schedule;

import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.schedulesessionallocation.ScheduleSessionAllocation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ScheduleSessionAllocationResponse {

    private UUID scheduleSessionAllocationId;
    private UUID scheduleId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private CourseSession.CourseSessionType courseSessionType;
    private UUID taCourseAssignmentId;

    public static ScheduleSessionAllocationResponse of(ScheduleSessionAllocation allocation) {
        return new ScheduleSessionAllocationResponse(
                allocation.getScheduleSessionAllocationId(),
                allocation.getSchedule().getScheduleId(),
                allocation.getStartDateTime(),
                allocation.getEndDateTime(),
                allocation.getCourseSessionType(),
                allocation.getTaCourseAssignment().getTaCourseAssignmentId()
        );
    }
}