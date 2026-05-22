import type {CourseAssignmentConstraintsForm, RankingState, TimeSlot} from "../types/taConstraintsPageTypes.ts";
import type {
    CourseAssignmentConstraintsRequest, CourseAssignmentConstraintsResponse,
    PutTAConstraintsTimeSlotsRequest,
    TAConstraintsTimeSlotsResponse
} from "../api/taConstraintsApi.ts";
import {SessionType} from "../Components/TA/SessionRanking1to4.tsx";
import type {CourseSessionType} from "../api/courseSessionsApi.ts";

export function buildRequest(
    formToSave: CourseAssignmentConstraintsForm
): CourseAssignmentConstraintsRequest {
    return {
        minHours: formToSave.minHours === "" ? 0 : Number(formToSave.minHours),
        maxHours: formToSave.maxHours === "" ? 0 : Number(formToSave.maxHours),
        sessionTypePreference1: formToSave.sessionTypePreference1,
        sessionTypePreference2: formToSave.sessionTypePreference2,
        sessionTypePreference3: formToSave.sessionTypePreference3,
        sessionTypePreference4: formToSave.sessionTypePreference4,
        isCompactSchedule: formToSave.isCompactSchedule,
    };
}

function toLocalDateInputValue(dateTime: string): string {
    return dateTime.slice(0, 10);
}

function toLocalTimeInputValue(dateTime: string): string {
    return dateTime.slice(11, 16);
}

function combineDateAndTime(date: string, time: string): string {
    return `${date}T${time}:00`;
}

export function mapResponseToTimeSlot(slot: TAConstraintsTimeSlotsResponse): TimeSlot {
    return {
        id: slot.taCourseSessionConstraintId,
        backendId: slot.taCourseSessionConstraintId,
        constraintType: slot.constraintType,
        date: toLocalDateInputValue(slot.startDateTime),
        startTime: toLocalTimeInputValue(slot.startDateTime),
        endTime: toLocalTimeInputValue(slot.endDateTime),
        isWeeklyRecurring: slot.isWeeklyRecurring,
    };
}

export function mapTimeSlotsToPutRequest(
    hardTimeSlots: TimeSlot[],
    softTimeSlots: TimeSlot[]
): PutTAConstraintsTimeSlotsRequest {
    const allSlots = [...hardTimeSlots, ...softTimeSlots];

    return {
        requests: allSlots.map((slot) => ({
            taCourseConstraintId: slot.backendId || undefined,
            constraintType: slot.constraintType,
            startDateTime: combineDateAndTime(slot.date, slot.startTime),
            endDateTime: combineDateAndTime(slot.date, slot.endTime),
            isWeeklyRecurring: slot.isWeeklyRecurring,
        })),
    };
}

export function preferencesToRanking(form: CourseAssignmentConstraintsForm): RankingState {
    const next: RankingState = {
        [SessionType.grading]: null,
        [SessionType.laboration]: null,
        [SessionType.help]: null,
        [SessionType.exercise]: null,
    };

    const prefMap = [
        form.sessionTypePreference1,
        form.sessionTypePreference2,
        form.sessionTypePreference3,
        form.sessionTypePreference4,
    ];

    prefMap.forEach((pref, index) => {
        if (pref === null) return;

        const key = pref.toLowerCase() as SessionType;
        next[key] = index + 1;
    });

    return next;
}

export function rankingToPreferences(ranking: RankingState): Pick<
    CourseAssignmentConstraintsForm,
    | "sessionTypePreference1"
    | "sessionTypePreference2"
    | "sessionTypePreference3"
    | "sessionTypePreference4"
> {
    const rankedItems: { type: CourseSessionType; rank: number }[] = [];

    for (const type in ranking) {
        const rank = ranking[type as SessionType];

        if (rank !== null) {
            rankedItems.push({
                type: type.toUpperCase() as CourseSessionType,
                rank,
            });
        }
    }

    rankedItems.sort((a, b) => a.rank - b.rank);

    return {
        sessionTypePreference1: rankedItems[0]?.type ?? null,
        sessionTypePreference2: rankedItems[1]?.type ?? null,
        sessionTypePreference3: rankedItems[2]?.type ?? null,
        sessionTypePreference4: rankedItems[3]?.type ?? null,
    };
}

export function hasSavedRanking(response: CourseAssignmentConstraintsResponse) {
    return (
        response.sessionTypePreference1 !== null &&
        response.sessionTypePreference2 !== null &&
        response.sessionTypePreference3 !== null &&
        response.sessionTypePreference4 !== null
    );
}

export function hasSavedSchedulePreference(response: CourseAssignmentConstraintsResponse) {
    return response.isCompactSchedule === true || response.isCompactSchedule === false;
}