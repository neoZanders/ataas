import SideTabNav from "../SideTabNav.tsx";
import {useAuth} from "../AuthContext.tsx";
import {useCurrentCourse} from "../CurrentCourseContext.tsx";
import {type CourseResponse, getCourseById} from "../../api/coursesApi.ts";
import type {TimeSlot} from "./AddTAConstraintsPopUp.tsx";
import {
    type CourseAssignmentConstraintsRequest, createTAConstraintNotASession, putTAConstraintsTimeSlots,
    type PutTAConstraintsTimeSlotsRequest,
    type TAConstraintsTimeSlotsResponse
} from "../../api/taConstraintsApi.ts";
import {Trash2} from "lucide-react";


function SectionShell({
                          title,
                          onRemove,
                          children,
                          className = "",
                      }: {
    title: string;
    onRemove: () => void;
    children: React.ReactNode;
    className?: string;
}) {
    return (
        <div className={`rounded-2xl border border-slate-200 bg-white p-4 ${className}`}>
            <div className="flex items-start justify-between gap-4">
                <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
                <button
                    type="button"
                    onClick={onRemove}
                    className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
                >
                    <Trash2 className="h-4 w-4" />
                    Remove
                </button>
            </div>
            <div className="mt-3">{children}</div>
        </div>
    );
}

export function TAConstraintspage2(){
    const { accessToken, user } = useAuth();
    const { currentCourseId } = useCurrentCourse();
    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [saveSuccess, setSaveSuccess] = useState<string | null>(null);

    const [hardTimeSlots, setHardTimeSlots] = useState<TimeSlot[]>([]);
    const [softTimeSlots, setSoftTimeSlots] = useState<TimeSlot[]>([]);

    const [form, setForm] = useState<CourseAssignmentConstraintsRequest>({
        minHours: 0,
        maxHours: 100,
        sessionTypePreference1: "LABORATION",
        sessionTypePreference2: "LABORATION",
        sessionTypePreference3: "LABORATION",
        sessionTypePreference4: "LABORATION",
        isCompactSchedule: false,
    });

    // from backend res to UI
    function mapResponseToTimeSlot(slot: TAConstraintsTimeSlotsResponse): TimeSlot {
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

    // from UI to backend
    function mapTimeSlotsToPutRequest(
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

    function toLocalDateInputValue(dateTime: string): string {
        return dateTime.slice(0, 10);
    }

    function toLocalTimeInputValue(dateTime: string): string {
        return dateTime.slice(11, 16);
    }

    function combineDateAndTime(date: string, time: string): string {
        return `${date}T${time}:00`;
    }

    const handleSaveConstraints = async () => {
        if (!currentCourseId) {
            setSaveError("No course selected, go to courses and select a course!");
            return;
        }

        if (!user?.id) {
            setSaveError("No user found.");
            return;
        }

        setIsSaving(true);
        setSaveError(null);
        setSaveSuccess(null);

        try {
            const response = await createTAConstraintNotASession(
                form,
                currentCourseId,
                user.id,
                accessToken
            );

            setForm({
                minHours: response.minHours,
                maxHours: response.maxHours,
                sessionTypePreference1: response.sessionTypePreference1,
                sessionTypePreference2: response.sessionTypePreference2,
                sessionTypePreference3: response.sessionTypePreference3,
                sessionTypePreference4: response.sessionTypePreference4,
                isCompactSchedule: response.isCompactSchedule,
            });

            const timeSlotRequest = mapTimeSlotsToPutRequest(hardTimeSlots, softTimeSlots);

            const savedTimeSlots = await putTAConstraintsTimeSlots(
                currentCourseId,
                accessToken,
                timeSlotRequest
            );

            const mappedSavedSlots = savedTimeSlots.map(mapResponseToTimeSlot);

            setHardTimeSlots(
                mappedSavedSlots.filter((slot) => slot.constraintType === "HARD")
            );
            setSoftTimeSlots(
                mappedSavedSlots.filter((slot) => slot.constraintType === "SOFT")
            );

            setSaveSuccess("Constraints saved.");
        } catch (error) {
            console.error("Failed to save constraints", error);
            setSaveError("Could not save constraints.");
        } finally {
            setIsSaving(false);
        }
    };

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

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen py-4 pl-[104px]">
                <h1 className="text-3xl text-center font-bold text-slate-900">TA Constraints {course?.courseCode}</h1>

                {/*  HARD CONSTRAINTS  */}
                <section className="rounded-3xl bg-white p-6 mt-2 shadow-sm ring-1 ring-slate-200">
                    <h1 className=" text-xl text-center font-bold text-slate-900">
                    Input your hard constraints below:
                    </h1>
                    <button
                        className="mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center underline hover:text-slate-700"
                        onClick={() => {}}>
                        Description of hard constraints usage
                    </button>

                    <section
                    className="rounded-3xl bg-red-50 p-6 shadow-sm ring-1 ring-slate-200">
                    <h2 className="text-lg font-semibold text-slate-900">
                        Weekly working hours (Mandatory)
                    </h2>
                    <p className="mt-1 text-sm text-slate-500">
                        Set your preferred minimum and maximum number of working hours.
                    </p>

                    {saveError && (
                    <p className="mt-4 text-sm text-red-600">{saveError}</p>
                    )}

                    {saveSuccess && (
                        <p className="mt-4 text-sm text-emerald-600">{saveSuccess}</p>
                    )}
                        <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <label className="mb-2 block text-sm font-medium text-slate-700">
                                    Minimum hours
                                </label>

                                <div className="flex items-center gap-3">
                                    <input
                                        value={form.minHours}
                                        onChange={(e) =>
                                            setForm((prev) => ({
                                                ...prev,
                                                minHours: Number(e.target.value),
                                            }))
                                        }
                                        className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                    />
                                    <span className="text-sm text-slate-600">hours</span>
                                </div>
                            </div>

                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <label className="mb-2 block text-sm font-medium text-slate-700">
                                    Maximum hours
                                </label>

                                <div className="flex items-center gap-3">
                                    <input
                                        value={form.maxHours}
                                        onChange={(e) =>
                                            setForm((prev) => ({
                                                ...prev,
                                                maxHours: Number(e.target.value),
                                            }))
                                        }
                                        className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                    />
                                    <span className="text-sm text-slate-600">hours</span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                type="button"
                                onClick={handleSaveConstraints}
                                disabled={isSaving}
                                className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                            >
                                {isSaving ? "Saving..." : "Save hours"}
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-green-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Timeslots you can't work</h2>
                        <p className="mt-1 text-sm text-slate-500">No timeslots added yet, add timeslots?</p>
                        <div className="mt-6 flex justify-end">
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                            onClick={() => {}}
                            >
                            Add timeslot
                            </button>
                        </div>
                    </section>
                </section>

                {/*  Soft Constraints */}
                <section className="rounded-3xl bg-white p-6 mt-2 shadow-sm ring-1 ring-slate-200">
                    <h1 className=" text-xl text-center font-bold text-slate-900">
                        Input your soft constraints below:
                    </h1>
                    <button
                        className="mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center underline hover:text-slate-700"
                        onClick={() => {}}>
                        Description of soft constraints usage
                    </button>

                    <section className="rounded-3xl mt-4 bg-green-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Timeslots you prefer not to work</h2>
                        <p className="mt-1 text-sm text-slate-500">No timeslots added yet, add timeslots?</p>
                        <div className="mt-6 flex justify-end">
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {}}
                            >
                                Add timeslot
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-green-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Session type ranking</h2>
                        <p className="mt-1 text-sm text-slate-500">No session ranking added yet, add session preference between lab, grading, exercise and help session types?</p>
                        <div className="mt-6 flex justify-end">
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {}}
                            >
                                Add ranking
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-green-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Compact or spread out schedule</h2>
                        <p className="mt-1 text-sm text-slate-500">No choice selected yet, add preference for a compact schedule with more sessions during a single day or a spread out schedule over the week?</p>
                        <div className="mt-6 flex justify-end">
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {}}
                            >
                                Add ranking
                            </button>
                        </div>
                    </section>

                </section>
            </main>
        </div>
    )
}