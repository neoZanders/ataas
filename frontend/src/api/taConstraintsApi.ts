import { authFetchJson } from "./authFetch.ts";
import type { UserResponse} from "./authApi.ts";
import type {CourseSessionType} from "./courseSessionsApi.ts";
import type {CrCourseAssignment, TaCourseAssignment} from "./courseAssignmentApi.ts";


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
    isWeeklyRecurring: boolean;
}

export type GetCourseAssignmentConstraintsResponse = {
    courseId: string;
    crCourseAssignments: CrCourseAssignment[];
    taCourseAssignments: TaCourseAssignment[];
}


export type PutTAConstraintsTimeSlotRequest = {
    taCourseConstraintId?: string;
    constraintType: "SOFT" | "HARD";
    startDateTime: string;
    endDateTime: string;
    isWeeklyRecurring: boolean;
}

export type PutTAConstraintsTimeSlotsRequest = {
    requests: PutTAConstraintsTimeSlotRequest[];
}

export type GetTAConstraintsTimeSlotResponse = {
    ta: UserResponse;
    taConstraints: TAConstraintsTimeSlotsResponse[];
}

export async function getAllTAConstraintsTimeSlots(
    courseId: string,
    accessToken: string | null
): Promise<GetTAConstraintsTimeSlotResponse[]>{
    return authFetchJson<GetTAConstraintsTimeSlotResponse[]>(
        `/api/courses/${courseId}/ta-constraints`, accessToken, {
            method: "GET",
        });
}

export async function getCourseAssignmentConstraintResponse(
    courseId: string,
    accessToken: string | null
): Promise<GetCourseAssignmentConstraintsResponse>{
    return authFetchJson<GetCourseAssignmentConstraintsResponse>(
        `/api/courses/${courseId}/course-assignments`, accessToken, {
            method: "GET",
        });
}

export async function putTAConstraintsTimeSlots(
    courseId: string,
    accessToken: string | null,
    req: PutTAConstraintsTimeSlotsRequest,
): Promise<TAConstraintsTimeSlotsResponse[]>{
    return authFetchJson<TAConstraintsTimeSlotsResponse[]>(`/api/courses/${courseId}/ta-constraints`,
        accessToken, {
            method: "PUT",
            body: JSON.stringify(req)
        });
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
