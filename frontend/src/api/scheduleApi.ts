import { authFetchJson } from "./authFetch.ts";

export type ScheduleSessionAllocationResponse = {
    scheduleSessionAllocationsId: string;
    scheduleId: string;
    courseSessionId: string;
    taCourseAssignmentId: string;
};

export type ScheduleResponse = {
    scheduleId: string;
    courseId: string;
    allocations: ScheduleSessionAllocationResponse[];
};

export async function getSchedule(
    courseId: string,
    accessToken: string | null
): Promise<ScheduleResponse> {
    return authFetchJson<ScheduleResponse>(`/api/courses/${courseId}/schedule`, accessToken, {
        method: "GET",
    });
}
