import type { EventInput } from "@fullcalendar/core";
import { useEffect, useMemo, useState } from "react";
import { getCourseSessions, type CourseSessionResponse, type CourseSessionType } from "../../api/courseSessionsApi.ts";
import { ApiError } from "../../api/http.ts";
import { getSchedule } from "../../api/scheduleApi.ts";
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

export function TAMainPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();

    const [courseSessions, setCourseSessions] = useState<CourseSessionResponse[]>([]);
    const [visibleCourseSessionIds, setVisibleCourseSessionIds] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!currentCourseId || !accessToken) {
            setCourseSessions([]);
            setVisibleCourseSessionIds([]);
            setIsLoading(false);
            setError(null);
            return;
        }

        let isMounted = true;

        const loadSchedule = async () => {
            setIsLoading(true);
            setError(null);

            try {
                const [schedule, sessions] = await Promise.all([
                    getSchedule(currentCourseId, accessToken),
                    getCourseSessions(currentCourseId, accessToken),
                ]);

                if (!isMounted) {
                    return;
                }

                setCourseSessions(sessions);
                setVisibleCourseSessionIds(
                    Array.from(new Set(schedule.allocations.map((allocation) => allocation.courseSessionId)))
                );
            } catch (loadError) {
                console.error("Failed to load TA schedule", loadError);

                if (!isMounted) {
                    return;
                }

                if (loadError instanceof ApiError && loadError.status === 404) {
                    setCourseSessions([]);
                    setVisibleCourseSessionIds([]);
                    setError(null);
                } else {
                    setCourseSessions([]);
                    setVisibleCourseSessionIds([]);
                    setError("Could not load schedule.");
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadSchedule();

        return () => {
            isMounted = false;
        };
    }, [currentCourseId, accessToken]);

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

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />
            <main className="pl-[104px] pt-6">
                <div className="mx-auto w-full max-w-7xl px-6">
                    {error ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-rose-600 shadow-sm ring-1 ring-slate-200">
                            {error}
                        </div>
                    ) : isLoading ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            Loading schedule...
                        </div>
                    ) : !currentCourseId ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            Select a workspace to view your schedule.
                        </div>
                    ) : events.length === 0 ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            No schedule available for this workspace.
                        </div>
                    ) : (
                        <Calendar events={events} />
                    )}
                </div>
            </main>
        </div>
    );
}
