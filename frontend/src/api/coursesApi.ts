import type {UserResponse} from "./authApi.ts";
import {authFetchJson} from "./authFetch.ts";

export type CourseRequest = {
    courseCode: string;
    description: string;
    canTASeeAllSchedules: boolean;
    canTACreateAnnouncements: boolean;
    startDate: string; // ISO
    endDate: string;   // ISO
}

export type CourseResponse = {
    courseId: string;
    courseCode: string;
    owner: UserResponse;
    description: string,
    status: "ACTIVE" | "ARCHIVED" | string;
    canTASeeAllSchedules: boolean,
    canTACreateAnnouncements: boolean,
    startDate: string;
    endDate: string;
}

export type GetCoursesResponse = {
    course: CourseResponse;
    assignmentStatus: "OWNER" | "JOINED" | "INVITED" | string; };

export async function createCourse(
    req: CourseRequest,
    accessToken: string | null
): Promise<CourseResponse> {
    return authFetchJson<CourseResponse>("/api/courses", accessToken, {
        method: "POST",
        body: JSON.stringify(req),
    });
}

export async function getCourses(
    accessToken: string | null
): Promise<GetCoursesResponse[]>
{
    return authFetchJson<GetCoursesResponse[]>("/api/courses", accessToken, {
        method: "GET",
    });
}

export async function getCourseById(
    courseId: string,
    accessToken: string | null
): Promise<CourseResponse> {
    return authFetchJson<CourseResponse>(`/api/courses/${courseId}/details`, accessToken, {
        method: "GET",
    });
}

export async function archiveCourse(
    courseId: string,
    accessToken: string | null
): Promise<void> {
    return authFetchJson<void>(`/api/courses/${courseId}/archive`, accessToken, {
        method: "PUT",
    });
}

export async function deleteCourse(
    courseId: string,
    accessToken: string | null
): Promise<void> {
    return authFetchJson<void>(`/api/courses/${courseId}`, accessToken, {
        method: "DELETE",
    });
}

export async function joinCourse(
    accessToken: string | null,
    courseId: string
)  : Promise<void> {
    return authFetchJson<void>(`/api/courses/${courseId}/course-assignments/join`, accessToken, {
        method: "POST",
    })
}