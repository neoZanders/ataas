import { authFetchJson } from "./authFetch.ts";

export type CourseSessionType = "GRADING" | "LABORATION" | "HELP" | "EXERCISE";

export type CourseSessionResponse = {
    courseSessionId: string;
    courseId: string;
    startDateTime: string;
    endDateTime: string;
    courseSessionType: CourseSessionType;
    minTAs: number;
    maxTAs: number;
    isWeeklyRecurring: boolean;
};

export type CreateCourseSessionRequest = {
    startDateTime: string;
    endDateTime: string;
    courseSessionType: CourseSessionType;
    minTAs: number;
    maxTAs: number;
    isWeeklyRecurring: boolean;
}

export type CreateCourseSessionResponse = {
    courseSessionId: string;
    courseId: string;
    startDateTime: string;
    endDateTime: string;
    courseSessionType: CourseSessionType;
    minTAs: number;
    maxTAs: number;
    isWeeklyRecurring: boolean;
}

export async function getCourseSessions(
    courseId: string,
    accessToken: string | null
): Promise<CourseSessionResponse[]> {
    return authFetchJson<CourseSessionResponse[]>(
        `/api/courses/${courseId}/course-sessions`,
        accessToken,
        { method: "GET" }
    );
}

export async function createCourseSession(
    courseId: string,
    req: CreateCourseSessionRequest,
    accessToken: string | null
): Promise<CourseSessionResponse> {
    return authFetchJson<CreateCourseSessionResponse>(
        `/api/courses/${courseId}/course-sessions`,
        accessToken,
        {
            method: "POST",
            body: JSON.stringify(req),
        }
    );
}

export async function deleteCourseSession(
    courseId: string,
    courseSessionId: string,
    accessToken: string | null
): Promise<void> {
    return authFetchJson<void>(
        `/api/courses/${courseId}/course-sessions/${courseSessionId}`,
        accessToken,
        { method: "DELETE" }
    );
}