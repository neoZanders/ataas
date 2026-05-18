import type { EventInput } from "@fullcalendar/core";
import type { ScheduleResponse } from "../api/scheduleApi.ts";
import type { CourseSessionType } from "../api/courseSessionsApi.ts";

function sessionTypeLabel(type: CourseSessionType) {
    switch (type) {
        case "GRADING":
            return "Grading";
        case "LABORATION":
            return "Laboration";
        case "HELP":
            return "Help";
        case "EXERCISE":
            return "Exercise";
        default:
            return type;
    }
}

export function mapScheduleToEvents(schedule: ScheduleResponse, isCR: boolean): EventInput[] {
    if (schedule.canTAsSeeAllSchedules || isCR) {
        return schedule.allocations.map((allocation) => ({
            id: allocation.scheduleSessionAllocationId,
            title: `${sessionTypeLabel(allocation.courseSessionType)}`,
            start: allocation.startDateTime,
            end: allocation.endDateTime,
            extendedProps: {
                ta: allocation.taName
            }
        }));
    } else {
        return schedule.allocations.map((allocation) => ({
            id: allocation.scheduleSessionAllocationId,
            title: `${sessionTypeLabel(allocation.courseSessionType)}`,
            start: allocation.startDateTime,
            end: allocation.endDateTime
        }));
    }
}