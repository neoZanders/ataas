import type {CourseSessionType} from "../api/courseSessionsApi.ts";
import {SessionType} from "../Components/TA/SessionRanking1to4.tsx";

export type TimeSlot = {
    id: string;
    date: string;
    startTime: string;
    endTime: string;
    constraintType: "SOFT" | "HARD";
    backendId?: string;
    isWeeklyRecurring: boolean;
};

export type CourseAssignmentConstraintsForm = {
    minHours: string;
    maxHours: string;
    sessionTypePreference1: CourseSessionType | null;
    sessionTypePreference2: CourseSessionType | null;
    sessionTypePreference3: CourseSessionType | null;
    sessionTypePreference4: CourseSessionType | null;
    isCompactSchedule: boolean | null;
};

export type RankingState = Record<SessionType, number | null>;

export type SaveSection =
    | "hours"
    | "hardTimeslots"
    | "softTimeslots"
    | "ranking"
    | "schedule"
    | null;

export type CourseCodeRequest = {
    courseCode: string;
}