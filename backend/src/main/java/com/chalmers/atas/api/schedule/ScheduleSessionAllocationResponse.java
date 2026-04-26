package com.chalmers.atas.api.schedule;

import com.chalmers.atas.domain.schedulesessionallocation.ScheduleSessionAllocation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ScheduleSessionAllocationResponse {

    private UUID scheduleSessionAllocationId;
    private UUID scheduleId;
    private UUID courseSessionId;
    private UUID taCourseAssignmentId;

    public static ScheduleSessionAllocationResponse of(ScheduleSessionAllocation allocation) {
        return new ScheduleSessionAllocationResponse(
                allocation.getScheduleSessionAllocationId(),
                allocation.getSchedule().getScheduleId(),
                allocation.getCourseSession().getCourseSessionId(),
                allocation.getTaCourseAssignment().getTaCourseAssignmentId()
        );
    }
}
