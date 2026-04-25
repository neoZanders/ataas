import { authFetchJson } from "./authFetch.ts";
import type {User} from "../Components/AuthContext.tsx";
import type {CourseSessionType} from "./courseSessionsApi.ts";

export type CrCourseAssignment = {
    courseAssignmentId: string;
    status: "OWNER" | "JOINED" | "INVITED" | string;
    user: User;
}

export type ListCourseMembersResponse = {
    courseId: string;
    crCourseAssignments: CrCourseAssignment[];
    taCourseAssignments: TaCourseAssignment[];
}

export type TaCourseAssignment = {
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

export type TAInvitationRequest = {
    taEmail: string;
}

export type CRInvitationRequest = {
    crEmail: string;
}

export async function inviteTas(
    courseId: string,
    accessToken: string | null,
    req: TAInvitationRequest
): Promise<void> {
    return authFetchJson<void>(
        `/api/courses/${courseId}/course-assignments/ta-invitations`,
       accessToken,
        {method: "POST", body: JSON.stringify(req)}
    )
}

export async function inviteCrs(
    courseId: string,
    accessToken: string | null,
    req: CRInvitationRequest
): Promise<void> {
    return authFetchJson<void>(
        `/api/courses/${courseId}/course-assignments/cr-invitations`,
        accessToken,
        {method: "POST", body: JSON.stringify(req)}
    )
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
