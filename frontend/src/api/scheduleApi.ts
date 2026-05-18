import { authFetchJson } from "./authFetch.ts";
import type { CourseSessionType } from "./courseSessionsApi.ts";

export type ScheduleSessionAllocationResponse = {
    scheduleSessionAllocationId: string;
    startDateTime: string;
    endDateTime: string;
    courseSessionType: CourseSessionType;
    taCourseAssignmentId: string;
    taName: string;
};

export type ScheduleResponse = {
    scheduleId: string;
    courseId: string;
    canTAsSeeAllSchedules: boolean;
    allocations: ScheduleSessionAllocationResponse[];
};

export async function createSchedule(
    courseId: string,
    accessToken: string | null
): Promise<ScheduleResponse> {
    return authFetchJson<ScheduleResponse>(`/api/courses/${courseId}/schedule`, accessToken, {
        method: "POST",
    });
}

export async function getSchedule(
    courseId: string,
    accessToken: string | null
): Promise<ScheduleResponse> {
    return authFetchJson<ScheduleResponse>(`/api/courses/${courseId}/schedule`, accessToken, {
        method: "GET",
    });
}