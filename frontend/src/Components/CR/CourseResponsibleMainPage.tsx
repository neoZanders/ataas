import type { EventInput } from "@fullcalendar/core";
import { useCallback, useEffect, useMemo, useState } from "react";
import { getCourseSessions, type CourseSessionResponse, type CourseSessionType } from "../../api/courseSessionsApi.ts";
import { ApiError } from "../../api/http.ts";
import { createSchedule, getSchedule } from "../../api/scheduleApi.ts";
import { useAuth } from "../AuthContext.tsx";
import Calendar from "../Calendar.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import SideTabNav from "../SideTabNav.tsx";

function sessionTypeLabel(type: CourseSessionType) {
    switch (type) {
        case "GRADING":
            return "Grading";
        case "LABORATION":
            return "Laboration";
        case "HELP":
            return "Help";
        case "EXERCISE":
            return "Exercise";
        default:
            return type;
    }
}

export function CourseResponsibleMainPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();
    const [courseSessions, setCourseSessions] = useState<CourseSessionResponse[]>([]);
    const [visibleCourseSessionIds, setVisibleCourseSessionIds] = useState<string[]>([]);
    const [isLoadingSchedule, setIsLoadingSchedule] = useState(false);
    const [isRunningAlgorithm, setIsRunningAlgorithm] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const loadSchedule = useCallback(async () => {
        if (!currentCourseId || !accessToken) {
            setCourseSessions([]);
            setVisibleCourseSessionIds([]);
            setErrorMessage(null);
            return;
        }

        setIsLoadingSchedule(true);
        setErrorMessage(null);

        try {
            const [schedule, sessions] = await Promise.all([
                getSchedule(currentCourseId, accessToken),
                getCourseSessions(currentCourseId, accessToken),
            ]);

            setCourseSessions(sessions);
            setVisibleCourseSessionIds(
                Array.from(new Set(schedule.allocations.map((allocation) => allocation.courseSessionId)))
            );
        } catch (error) {
            console.error("Failed to load schedule", error);

            setCourseSessions([]);
            setVisibleCourseSessionIds([]);

            if (error instanceof ApiError && error.status === 404) {
                setErrorMessage(null);
            } else {
                setErrorMessage("Could not load schedule.");
            }
        } finally {
            setIsLoadingSchedule(false);
        }
    }, [currentCourseId, accessToken]);

    useEffect(() => {
        void loadSchedule();
    }, [loadSchedule]);

    const events = useMemo<EventInput[]>(() => {
        const visibleIds = new Set(visibleCourseSessionIds);

        return courseSessions
            .filter((session) => visibleIds.has(session.courseSessionId))
            .map((session) => ({
                id: session.courseSessionId,
                title: `Session: ${sessionTypeLabel(session.courseSessionType)}`,
                start: session.startDateTime,
                end: session.endDateTime,
            }));
    }, [courseSessions, visibleCourseSessionIds]);

    const handleRunAlgorithm = async () => {
        if (!currentCourseId) {
            setErrorMessage("Select a workspace before running the algorithm.");
            return;
        }

        setIsRunningAlgorithm(true);
        setErrorMessage(null);

        try {
            await createSchedule(currentCourseId, accessToken);
            await loadSchedule();
        } catch (error) {
            console.error("Failed to run algorithm", error);
            setErrorMessage("Could not run algorithm.");
        } finally {
            setIsRunningAlgorithm(false);
        }
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />
            <main className="pl-26 pt-6">
                <div className="mb-6 flex flex-col items-center justify-center gap-3">
                <button
                    className="rounded-2xl bg-[#003b5c] px-10 py-2 text-xl font-medium text-slate-50 hover:bg-[#002741] disabled:cursor-not-allowed disabled:opacity-60"
                    type="button"
                    onClick={handleRunAlgorithm}
                    disabled={isRunningAlgorithm || isLoadingSchedule}>
                    {isRunningAlgorithm ? "Running..." : "Run Algorithm"}
                </button>
                    {errorMessage && (
                        <p className="text-sm text-rose-600">{errorMessage}</p>
                    )}
                </div>
                <div className="mx-auto w-full max-w-7xl px-6">
                    {isLoadingSchedule || isRunningAlgorithm ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            {isRunningAlgorithm ? "Preparing schedule..." : "Loading schedule..."}
                        </div>
                    ) : !currentCourseId ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            Select a workspace to view its schedule.
                        </div>
                    ) : errorMessage ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-rose-600 shadow-sm ring-1 ring-slate-200">
                            {errorMessage}
                        </div>
                    ) : events.length === 0 ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            No schedule exists for this workspace.
                        </div>
                    ) : (
                        <Calendar events={events} />
                    )}
                </div>
            </main>
        </div>
    );
}
