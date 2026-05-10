import { useState } from "react";
import { createSchedule } from "../../api/scheduleApi.ts";
import { useAuth } from "../AuthContext.tsx";
import Calendar from "../Calendar.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import SideTabNav from "../SideTabNav.tsx";

export function CourseResponsibleMainPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();
    const [isRunningAlgorithm, setIsRunningAlgorithm] = useState(false);
    const [statusMessage, setStatusMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

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
            setStatusMessage(
                schedule.allocations.length > 0
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
                    className="rounded-2xl bg-[#003b5c] text-xl font-medium text-slate-50 hover:bg-[#002741] px-10 py-2 cursor-pointer"
                    type="button"
                    onClick={handleRunAlgorithm}
                    disabled={isRunningAlgorithm}>
                    {isRunningAlgorithm ? "Running..." : "Run Algorithm"}
                </button>
                    {statusMessage && (
                        <p className="text-sm text-emerald-700">{statusMessage}</p>
                    )}
                    {errorMessage && (
                        <p className="text-sm text-rose-600">{errorMessage}</p>
                    )}
                </div>
                <Calendar/>
            </main>
        </div>
    );
}
