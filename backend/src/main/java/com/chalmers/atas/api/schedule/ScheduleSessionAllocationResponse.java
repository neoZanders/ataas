package com.chalmers.atas.api.schedule;

import com.chalmers.atas.domain.schedule.ScheduleSessionAllocation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ScheduleSessionAllocationResponse {

    private UUID allocationId;
    private UUID courseId;
    private UUID sessionId;
    private UUID taId;

    public static ScheduleSessionAllocationResponse of(ScheduleSessionAllocation allocation) {
        return new ScheduleSessionAllocationResponse(
                allocation.getAllocationId(),
                allocation.getCourse().getCourseId(),
                allocation.getSession().getSessionId(),
                allocation.getTA().getUserId()
        );
    }
}
