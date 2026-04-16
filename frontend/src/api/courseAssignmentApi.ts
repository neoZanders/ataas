import { authFetchJson } from "./authFetch.ts";
import type {User} from "../Components/AuthContext.tsx";
import type {CourseSessionType} from "./courseSessionsApi.ts";

export type crCourseAssignment = {
    courseAssignmentId: string;
    status: "OWNER" | "JOINED" | "INVITED" | string;
    user: User;
}

export type ListCourseMembersResponse = {
    courseId: string;
    crCourseAssignments: crCourseAssignment[];
    taCourseAssignments: taCourseAssignment[];
}

export type taCourseAssignment = {
    taCourseAssignmentId: string;
    ta: User;
    status: "OWNER" | "JOINED" | "INVITED" | string;
    minHours: number;
    maxHours: number;
    sessionTypePreference1: CourseSessionType;
    sessionTypePreference2: CourseSessionType;
    sessionTypePreference3: CourseSessionType;
    sessionTypePreference4: CourseSessionType;
    isCompactSchedule: boolean;
}

export async function getListCourseMembers(
    courseId: string,
    accessToken: string | null
): Promise<ListCourseMembersResponse> {
    return authFetchJson<ListCourseMembersResponse>(
        `/api/courses/${courseId}/course-assignments`,
        accessToken,
        { method: "GET" }
    );
}
