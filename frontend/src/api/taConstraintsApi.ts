import { authFetchJson } from "./authFetch.ts";
import type { UserResponse} from "./authApi.ts";
import type {CourseSessionType} from "./courseSessionsApi.ts";


export type CourseAssignmentConstraintsRequest = {
    minHours: number;
    maxHours: number;
    sessionTypePreference1: CourseSessionType;
    sessionTypePreference2: CourseSessionType;
    sessionTypePreference3: CourseSessionType;
    sessionTypePreference4: CourseSessionType;
    isCompactSchedule: boolean;

}

export type CourseAssignmentConstraintsResponse = {
    taCourseAssignmentId: string;
    ta: UserResponse;
    status: "OWNER" | "JOINED" | "INVITED" | string;
    minHours: number;
    maxHours: number;
    sessionTypePreference1: CourseSessionType;
    sessionTypePreference2: CourseSessionType;
    sessionTypePreference3: CourseSessionType;
    sessionTypePreference4: CourseSessionType;
    isCompactSchedule: boolean;
}

export type TAConstraintsTimeSlotsResponse = {
    taCourseSessionConstraintId: string;
    courseId: string;
    taId: string;
    constraintType: "SOFT" | "HARD";
    startDateTime: string;
    endDateTime: string;
    weeklyRecurring: boolean;
}

export type TAConstraintsTimeSlotsRequest = {
    constraintType: "SOFT" | "HARD";
    startDateTime: string;
    endDateTime: string;
    isWeeklyRecurring: boolean;
}


export async function getCourseAssignmentConstraints(
    taId: string,
    courseId: string,
    accessToken: string | null,
):Promise<CourseAssignmentConstraintsResponse>
{
    return authFetchJson<CourseAssignmentConstraintsResponse>(
        `/api/courses/${courseId}/course-assignments/tas/${taId}/details`, accessToken, {
            method: "GET",
        });
}

export async function updateTAConstraintsTimeSlots(
    courseId: string,
    accessToken: string | null,
    taCourseSessionConstraintId: string,
    req: TAConstraintsTimeSlotsRequest
):Promise<TAConstraintsTimeSlotsResponse>{
    return authFetchJson<TAConstraintsTimeSlotsResponse>(`/api/courses/${courseId}/ta-constraints/${taCourseSessionConstraintId}`,
        accessToken, {
            method: "PATCH",
            body: JSON.stringify(req)
        });
}

export async function createTAConstraintsTimeSlots(
    courseId: string,
    accessToken: string | null,
    req: TAConstraintsTimeSlotsRequest,
):Promise<void> {
    return authFetchJson<void>(`/api/courses/${courseId}/ta-constraints`,
        accessToken, {
            method: "POST",
            body: JSON.stringify(req)
        });
}

export async function deleteTAConstraintsTimeSlots(
    courseId: string,
    taCourseSessionConstraintId: string,
    accessToken: string | null,
): Promise<void>
{
    return authFetchJson<void>(`/api/courses/${courseId}/ta-constraints/${taCourseSessionConstraintId}`,
        accessToken, {
        method: "DELETE",
    });
}

export async function getTAConstraintsTimeSlots(
    courseId: string,
    taId: string,
    accessToken: string | null
):Promise<TAConstraintsTimeSlotsResponse[]>
{
    return authFetchJson<TAConstraintsTimeSlotsResponse[]>(
        `/api/courses/${courseId}/ta-constraints/${taId}`, accessToken, {
        method: "GET",
    });
}

export async function createTAConstraintNotASession(
    req: CourseAssignmentConstraintsRequest,
    courseId: string,
    taId: string,
    accessToken: string | null
): Promise<CourseAssignmentConstraintsResponse>
{

    return authFetchJson<CourseAssignmentConstraintsResponse>(
        `/api/courses/${courseId}/course-assignments/tas/${taId}`, accessToken, {
        method: "PATCH",
        body: JSON.stringify(req)
    });
}
