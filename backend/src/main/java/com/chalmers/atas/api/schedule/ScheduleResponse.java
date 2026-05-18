package com.chalmers.atas.api.schedule;

import com.chalmers.atas.domain.schedule.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ScheduleResponse {

    private UUID scheduleId;
    private UUID courseId;
    private boolean canTAsSeeAllSchedules;
    private List<ScheduleSessionAllocationResponse> allocations;

    public static ScheduleResponse of(
            Schedule schedule,
            boolean canTAsSeeAllSchedules,
            List<ScheduleSessionAllocationResponse> allocations
    ) {
        return new ScheduleResponse(
                schedule.getScheduleId(),
                schedule.getCourse().getCourseId(),
                canTAsSeeAllSchedules,
                allocations
        );
    }
}
