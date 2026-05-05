import {
    Search,
    UserCircle2,
} from "lucide-react";
import SideTabNav from "../SideTabNav.tsx";
import {useAuth} from "../AuthContext.tsx";
import {useCurrentCourse} from "../CurrentCourseContext.tsx";
import {type CourseResponse, getCourseById} from "../../api/coursesApi.ts";
import {useEffect, useState} from "react";
import type {TimeSlot} from "../TA/AddTAConstraintsPopUp.tsx";
import {
    getAllTAConstraintsTimeSlots,
    getCourseAssignmentConstraintResponse,
    type GetCourseAssignmentConstraintsResponse,
    type GetTAConstraintsTimeSlotResponse,
    type TAConstraintsTimeSlotsResponse
} from "../../api/taConstraintsApi.ts";

type TAConstraints = {
        name: string;
        id: string;
        minHours: number;
        maxHours: number;
        hardTimeSlotConstraints: TimeSlot[];
}

export function CourseResponsibleConstraintsPage() {
    const { accessToken, user } = useAuth();
    const { currentCourseId } = useCurrentCourse()
    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [taConstraints, setTaConstraints] = useState<TAConstraints[]>([]);

    function mapResponseToTimeSlot(slot: TAConstraintsTimeSlotsResponse): TimeSlot {
        return {
            id: slot.courseId,
            backendId: slot.taCourseSessionConstraintId,
            constraintType: slot.constraintType,
            date: toLocalDateInputValue(slot.startDateTime),
            startTime: toLocalTimeInputValue(slot.startDateTime),
            endTime: toLocalTimeInputValue(slot.endDateTime),
            isWeeklyRecurring: slot.isWeeklyRecurring,
        };
    }

    function mapResponseToTAConstraints(response: GetTAConstraintsTimeSlotResponse, responseHours: GetCourseAssignmentConstraintsResponse): TAConstraints {
        const taAssignment = responseHours.taCourseAssignments.find(
            (assignment) => assignment.ta.email === response.ta.email
        )

        return {
            name: response.ta.name,
            id: response.ta.userId,
            minHours: taAssignment?.minHours ?? 0,
            maxHours: taAssignment?.maxHours ?? 100,
            hardTimeSlotConstraints: response.taConstraints
                .filter((slot) => slot.constraintType === "HARD")
                .map(mapResponseToTimeSlot)
        }
    }

    function toLocalDateInputValue(dateTime: string): string {
        return dateTime.slice(0, 10);
    }

    function toLocalTimeInputValue(dateTime: string): string {
        return dateTime.slice(11, 16);
    }

    useEffect(() => {
        const loadCourse = async () => {
            if (!currentCourseId || !accessToken) {
                setCourse(null);
                return;
            }
            try {
                const fetchedCourse = await getCourseById(currentCourseId, accessToken);
                setCourse(fetchedCourse);
            } catch (error){
                console.error("Failed to load course", error);
                setCourse(null);
            }
        };
        loadCourse();
    }, [currentCourseId, accessToken]);

    useEffect(() => {
        const loadTimeSlotConstraints = async () => {
            if (!currentCourseId || !accessToken || !user?.id) return;

            try {
                const response = await getAllTAConstraintsTimeSlots(
                    currentCourseId,
                    accessToken
                )

                const responseHours = await getCourseAssignmentConstraintResponse(
                    currentCourseId,
                    accessToken
                )

                const taConstraints = response.map(
                    (taResponse) => mapResponseToTAConstraints(taResponse, responseHours)
                )

                setTaConstraints(taConstraints);

            } catch (error) {
                console.error("Failed to load hard timeslot constraints", error);
            }
        };

        loadTimeSlotConstraints();
    }, [currentCourseId, accessToken, user?.id]);

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">Constraints</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Course responsible TA constraints overview
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"></div>

                        <div className="rounded-2xl border border-slate-200 overflow-hidden">
                            <div className="bg-slate align-top border-r border-slate-200">
                                <div className="bg-slate-50 border-b border-slate-200 px-5 py-4 h-28 space-y-3">
                                    <p className="flex w-full items-center gap-2 text-left text-sm font-semibold text-slate-700">
                                        TA Constraints
                                    </p>

                                    <div className="relative">
                                        <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                                            <Search className="h-4 w-4" />
                                        </span>
                                        <input
                                            type="text"
                                            placeholder="Search for TA..."
                                            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-3 text-sm font-normal text-slate-700 placeholder:text-slate-400 outline-none"
                                        />
                                    </div>
                                </div>

                                {taConstraints.map((row) => (
                                    <div key={row.id} className="rounded-3xl border border-r border-slate-200 bg-white p-4 m-2">
                                        <div className="flex items-center gap-3 text-sm font-semibold text-slate-900">
                                            <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                                <UserCircle2 className="h-5 w-5" />
                                            </span>

                                            {row.name}
                                        </div>
                                        <div className="mt-2 flex flex-wrap gap-2 pl-1">
                                            {row.hardTimeSlotConstraints.map((c, i) => (
                                                <span
                                                    key={i}
                                                    className="px-3 py-1 rounded-full bg-slate-50 text-slate-700 text-xs font-medium"
                                                >
                                                {c.date}, {c.startTime} - {c.endTime}, {c.isWeeklyRecurring ? "Weekly Recurring" : "One time"}
                                                </span>
                                            ))}
                                            <span
                                            className="px-3 py-1 rounded-full bg-green-100 text-slate-700 text-xs font-medium"
                                            >
                                                Maximum hours: {row.maxHours}
                                            </span>
                                            <span
                                                className="px-3 py-1 rounded-full bg-fuchsia-100 text-slate-700 text-xs font-medium"
                                            >
                                                Minimum hours: {row.minHours}
                                            </span>
                                        </div>

                                    </div>
                                ))}
                                </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    );
}