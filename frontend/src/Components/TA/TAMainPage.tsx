import type { EventInput } from "@fullcalendar/core";
import { useEffect, useState } from "react";
import { ApiError } from "../../api/http.ts";
import { getSchedule } from "../../api/scheduleApi.ts";
import { mapScheduleToEvents } from "../../utils/scheduleCalendarMapper.ts";
import { useAuth } from "../AuthContext.tsx";
import Calendar from "../Calendar.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import SideTabNav from "../SideTabNav.tsx";

export function TAMainPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();

    const [events, setEvents] = useState<EventInput[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!currentCourseId || !accessToken) {
            setEvents([]);
            setIsLoading(false);
            setError(null);
            return;
        }

        let isMounted = true;

        const loadSchedule = async () => {
            setIsLoading(true);
            setError(null);

            try {
                const schedule = await getSchedule(currentCourseId, accessToken);

                if (!isMounted) {
                    return;
                }

                setEvents(mapScheduleToEvents(schedule));
            } catch (loadError) {
                console.error("Failed to load TA schedule", loadError);

                if (!isMounted) {
                    return;
                }

                if (loadError instanceof ApiError && loadError.status === 404) {
                    setEvents([]);
                    setError(null);
                } else {
                    setEvents([]);
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