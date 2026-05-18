import type { EventInput } from "@fullcalendar/core";
import { useEffect, useState } from "react";
import { createSchedule, getSchedule } from "../../api/scheduleApi.ts";
import { ApiError } from "../../api/http.ts";
import { mapScheduleToEvents } from "../../utils/scheduleCalendarMapper.ts";
import { useAuth } from "../AuthContext.tsx";
import Calendar from "../Calendar.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import SideTabNav from "../SideTabNav.tsx";

export function CourseResponsibleMainPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();

    const [events, setEvents] = useState<EventInput[]>([]);
    const [isLoadingSchedule, setIsLoadingSchedule] = useState(false);
    const [isRunningAlgorithm, setIsRunningAlgorithm] = useState(false);
    const [statusMessage, setStatusMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        if (!currentCourseId || !accessToken) {
            setEvents([]);
            setIsLoadingSchedule(false);
            setStatusMessage(null);
            setErrorMessage(null);
            return;
        }

        let isMounted = true;

        const loadExistingSchedule = async () => {
            setIsLoadingSchedule(true);
            setErrorMessage(null);
            setStatusMessage(null);

            try {
                const schedule = await getSchedule(currentCourseId, accessToken);

                if (!isMounted) {
                    return;
                }

                setEvents(mapScheduleToEvents(schedule, true));
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                if (error instanceof ApiError && error.status === 404) {
                    setEvents([]);
                    setErrorMessage(null);
                } else {
                    console.error("Failed to load schedule", error);
                    setEvents([]);
                    setErrorMessage("Could not load existing schedule.");
                }
            } finally {
                if (isMounted) {
                    setIsLoadingSchedule(false);
                }
            }
        };

        void loadExistingSchedule();

        return () => {
            isMounted = false;
        };
    }, [currentCourseId, accessToken]);

    const handleRunAlgorithm = async () => {
        if (!currentCourseId) {
            setErrorMessage("Select a workspace before running the algorithm.");
            setStatusMessage(null);
            return;
        }

        setIsRunningAlgorithm(true);
        setErrorMessage(null);
        setStatusMessage(null);

        try {
            const schedule = await createSchedule(currentCourseId, accessToken);
            const scheduleEvents = mapScheduleToEvents(schedule, true);

            setEvents(scheduleEvents);

            setStatusMessage(
                scheduleEvents.length > 0
                    ? "Schedule generated successfully."
                    : "Schedule generated, but no allocations were returned."
            );
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

            <main className="pl-[104px] pt-6">
                <div className="mb-6 flex flex-col items-center justify-center gap-3">
                    <button
                        className="cursor-pointer rounded-2xl bg-[#003b5c] px-10 py-2 text-xl font-medium text-slate-50 hover:bg-[#002741] disabled:cursor-not-allowed disabled:opacity-60"
                        type="button"
                        onClick={handleRunAlgorithm}
                        disabled={isRunningAlgorithm || isLoadingSchedule}
                    >
                        {isRunningAlgorithm ? "Running..." : "Run Algorithm"}
                    </button>

                    {statusMessage && (
                        <p className="text-sm text-emerald-700">{statusMessage}</p>
                    )}

                    {errorMessage && (
                        <p className="text-sm text-rose-600">{errorMessage}</p>
                    )}
                </div>

                <div className="mx-auto w-full max-w-7xl px-6">
                    {isLoadingSchedule ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            Loading schedule...
                        </div>
                    ) : events.length === 0 ? (
                        <div className="rounded-2xl bg-white p-8 text-center text-sm text-slate-500 shadow-sm ring-1 ring-slate-200">
                            No schedule has been generated yet.
                        </div>
                    ) : (
                        <Calendar events={events} />
                    )}
                </div>
            </main>
        </div>
    );
}