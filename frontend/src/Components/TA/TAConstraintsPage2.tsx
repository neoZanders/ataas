import SideTabNav from "../SideTabNav.tsx";
import {useAuth} from "../AuthContext.tsx";
import {useCurrentCourse} from "../CurrentCourseContext.tsx";
import {useEffect, useMemo, useState} from "react";
import {type CourseResponse, getCourseById} from "../../api/coursesApi.ts";
import {
    type CourseAssignmentConstraintsRequest, createTAConstraintNotASession,
    getCourseAssignmentConstraints, getTAConstraintsTimeSlots, putTAConstraintsTimeSlots,
    type PutTAConstraintsTimeSlotsRequest,
    type TAConstraintsTimeSlotsResponse
} from "../../api/taConstraintsApi.ts";
import {Trash2} from "lucide-react";
import SessionTypeRanker, {SessionType} from "./SessionRanking1to4.tsx";

export type TimeSlot = {
    id: string;
    date: string;
    startTime: string;
    endTime: string;
    constraintType: "SOFT" | "HARD";
    backendId?: string;
    isWeeklyRecurring: boolean;
};

type RankingState = Record<SessionType, number | null>;

export function TAConstraintsPage2(){
    const { accessToken, user } = useAuth();
    const { currentCourseId } = useCurrentCourse();
    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [isSaving, setIsSaving] = useState(false);
    const [isSavingTimeSlots, setIsSavingTimeSlots] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [saveSuccess, setSaveSuccess] = useState<string | null>(null);

    const [hardTimeSlots, setHardTimeSlots] = useState<TimeSlot[]>([]);
    const [softTimeSlots, setSoftTimeSlots] = useState<TimeSlot[]>([]);

    const [hasAddedRanking, setHasAddedRanking] = useState<boolean>(false);
    const [hasAddedSchedulePreference, setHasAddedSchedulePreference] = useState<boolean>(false);

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

    function preferencesToRanking(form: CourseAssignmentConstraintsRequest): RankingState {
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
            if (pref === null){
                pref="GRADING"
            }
            const key = pref.toLowerCase() as SessionType;
            next[key] = index + 1;
        });

        return next;
    }

    function rankingToPreferences(ranking: RankingState) {
        const rankedItems: { type: string; rank: number }[] = [];

        for (const type in ranking) {
            const rank = ranking[type as SessionType];

            if (rank !== null) {
                rankedItems.push({
                    type: type.toUpperCase(),
                    rank: rank,
                });
            }
        }
        rankedItems.sort((a, b) => a.rank - b.rank);
        return {
            sessionTypePreference1:
                (rankedItems[0]?.type ?? "LABORATION") as CourseAssignmentConstraintsRequest["sessionTypePreference1"],

            sessionTypePreference2:
                (rankedItems[1]?.type ?? "HELP") as CourseAssignmentConstraintsRequest["sessionTypePreference2"],

            sessionTypePreference3:
                (rankedItems[2]?.type ?? "EXERCISE") as CourseAssignmentConstraintsRequest["sessionTypePreference3"],

            sessionTypePreference4:
                (rankedItems[3]?.type ?? "GRADING") as CourseAssignmentConstraintsRequest["sessionTypePreference4"],
        };
    }

    const ranking = useMemo(() => preferencesToRanking(form), [form]);

    function toLocalDateInputValue(dateTime: string): string {
        return dateTime.slice(0, 10);
    }

    function toLocalTimeInputValue(dateTime: string): string {
        return dateTime.slice(11, 16);
    }

    function combineDateAndTime(date: string, time: string): string {
        return `${date}T${time}:00`;
    }

    const removeHardTimeSlotRow = (id: string) => {
        setHardTimeSlots((prev) => prev.filter((slot) => slot.id !== id));
    };

    const removeSoftTimeSlotRow = (id: string) => {
        setSoftTimeSlots((prev) => prev.filter((slot) => slot.id !== id));
    };

    const updateHardTimeSlot = (id: string, updates: Partial<TimeSlot>) => {
        setHardTimeSlots((prev) =>
            prev.map((slot) => (slot.id === id ? { ...slot, ...updates } : slot))
        );
    };

    const updateSoftTimeSlot = (id: string, updates: Partial<TimeSlot>) => {
        setSoftTimeSlots((prev) =>
            prev.map((slot) => (slot.id === id ? {...slot, ...updates} : slot))
        );
    };

    const addHardTimeSlotRow = () => {
        setHardTimeSlots((prev) => [
            ...prev,
            {
                id: crypto.randomUUID(),
                date: "",
                startTime: "",
                endTime: "",
                constraintType: "HARD",
                isWeeklyRecurring: false,
            }
        ]);
    };

    const addSoftTimeSlotRow = () => {
        setSoftTimeSlots((prev) => [
            ...prev,
            {
                id: crypto.randomUUID(),
                date: "",
                startTime: "",
                endTime: "",
                constraintType: "SOFT",
                isWeeklyRecurring: false,
            }
        ]);
    };

    const handleSaveConstraintsNotTimeslots = async () => {
        if (!currentCourseId) {
            setSaveError("No course selected, go to courses and select a course!");
            return;
        }

        if (!user?.id) {
            setSaveError("No user found.");
            return;
        }

        if (!accessToken) {
            setSaveError("You are not logged in. Please log in again.");
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

            setSaveSuccess("Constraints saved.");
        } catch (error) {
            console.error("Failed to save constraints", error);
            setSaveError("Could not save constraints.");
        } finally {
            setIsSaving(false);
        }
    };

    const handleSaveConstraintsTimeSlots = async () => {
        if (!currentCourseId) {
            setSaveError("No course selected, go to courses and select a course!");
            return;
        }
        if (!accessToken) {
            setSaveError("You are not logged in. Please log in again.");
            return;
        }
        setIsSavingTimeSlots(true);
        setSaveError(null);
        setSaveSuccess(null);

        try {
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
        setIsSavingTimeSlots(false);
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

    useEffect(() => {
        const loadConstraints = async () => {
            if (!currentCourseId || !accessToken || !user?.id) {
                return;
            }

            try {
                const response = await getCourseAssignmentConstraints(
                    user.id,
                    currentCourseId,
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
            } catch (error) {
                console.error("Failed to load TA constraints", error);
            }
        };

        loadConstraints();
    }, [currentCourseId, accessToken, user?.id]);

    useEffect(() => {
        const loadTimeSlotConstraints = async () => {
            if (!currentCourseId || !accessToken || !user?.id) return;

            try {
                const response = await getTAConstraintsTimeSlots(
                    currentCourseId,
                    user.id,
                    accessToken
                );

                const mappedSlots = response.map(mapResponseToTimeSlot);

                setHardTimeSlots(
                    mappedSlots.filter((slot) => slot.constraintType === "HARD")
                );
                setSoftTimeSlots(
                    mappedSlots.filter((slot) => slot.constraintType === "SOFT")
                );
            } catch (error) {
                console.error("Failed to load timeslot constraints", error);
            }
        };

        loadTimeSlotConstraints();
    }, [currentCourseId, accessToken, user?.id]);


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
                                    {/*    TODO: fix value bug ! */}
                                    <input
                                        value={form.minHours}
                                        type="number"
                                        min={0}
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
                                        type="number"
                                        min={0}
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
                                onClick={handleSaveConstraintsNotTimeslots}
                                disabled={isSaving}
                                className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                            >
                                {isSaving ? "Saving..." : "Save hours"}
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Timeslots you can't work</h2>
                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hardTimeSlots.length > 0 ? "Add more timeslots or save?" : "No timeslots added yet, add timeslots?"}
                        </p>
                        <div className="space-y-3">
                            {hardTimeSlots.map((slot) => (
                                <div
                                    key={slot.id}
                                    className="rounded-2xl border border-slate-200 bg-slate-50 p-3"
                                >
                                    <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
                                        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4 flex-1">
                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    Date
                                                </label>
                                                <input
                                                    type="date"
                                                    value={slot.date}
                                                    onChange={(e) =>
                                                        updateHardTimeSlot(slot.id, { date: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    Start time
                                                </label>
                                                <input
                                                    type="time"
                                                    value={slot.startTime}
                                                    onChange={(e) =>
                                                        updateHardTimeSlot(slot.id, { startTime: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    End time
                                                </label>
                                                <input
                                                    type="time"
                                                    value={slot.endTime}
                                                    onChange={(e) =>
                                                        updateHardTimeSlot(slot.id, { endTime: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div className="flex items-center pt-6">
                                                <label className="inline-flex items-center gap-2 text-sm text-slate-700">
                                                    <input
                                                        type="checkbox"
                                                        checked={slot.isWeeklyRecurring}
                                                        onChange={(e) =>
                                                            updateHardTimeSlot(slot.id, {
                                                                isWeeklyRecurring: e.target.checked,
                                                            })
                                                        }
                                                        className="h-4 w-4 rounded border-slate-300 text-[#003b5c] focus:ring-[#003b5c]"
                                                    />
                                                    Weekly recurring
                                                </label>
                                            </div>
                                        </div>

                                        <button
                                            type="button"
                                            onClick={() => removeHardTimeSlotRow(slot.id)}
                                            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
                                            aria-label="Remove timeslot"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="mt-6 flex justify-end">
                            {hardTimeSlots.length > 0 && (
                                <button className="mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => handleSaveConstraintsTimeSlots()}
                                >
                                    Save
                                </button>
                            )}
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                            onClick={addHardTimeSlotRow}
                            >
                            Add timeslot
                            </button>

                        </div>
                    </section>
                </section>

                {/*   Soft Constraints */}
                <section className="rounded-3xl bg-white p-6 mt-2 shadow-sm ring-1 ring-slate-200">
                    <h1 className=" text-xl text-center font-bold text-slate-900">
                        Input your soft constraints below:
                    </h1>
                    <button
                        className="mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center underline hover:text-slate-700"
                        onClick={() => {}}>
                        Description of soft constraints usage
                    </button>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Timeslots you prefer not to work</h2>
                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {softTimeSlots.length > 0 ? "Add timeslot or save timeslots?" : "No timeslots added yet, add timeslots?"}
                        </p>

                        <div className="space-y-3">
                            {softTimeSlots.map((slot) => (
                                <div
                                    key={slot.id}
                                    className="rounded-2xl border border-slate-200 bg-slate-50 p-3"
                                >
                                    <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
                                        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4 flex-1">
                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    Date
                                                </label>
                                                <input
                                                    type="date"
                                                    value={slot.date}
                                                    onChange={(e) =>
                                                        updateSoftTimeSlot(slot.id, { date: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    Start time
                                                </label>
                                                <input
                                                    type="time"
                                                    value={slot.startTime}
                                                    onChange={(e) =>
                                                        updateSoftTimeSlot(slot.id, { startTime: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div>
                                                <label className="mb-1 block text-xs font-medium text-slate-600">
                                                    End time
                                                </label>
                                                <input
                                                    type="time"
                                                    value={slot.endTime}
                                                    onChange={(e) =>
                                                        updateSoftTimeSlot(slot.id, { endTime: e.target.value })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div className="flex items-center pt-6">
                                                <label className="inline-flex items-center gap-2 text-sm text-slate-700">
                                                    <input
                                                        type="checkbox"
                                                        checked={slot.isWeeklyRecurring}
                                                        onChange={(e) =>
                                                            updateSoftTimeSlot(slot.id, {
                                                                isWeeklyRecurring: e.target.checked,
                                                            })
                                                        }
                                                        className="h-4 w-4 rounded border-slate-300 text-[#003b5c] focus:ring-[#003b5c]"
                                                    />
                                                    Weekly recurring
                                                </label>
                                            </div>
                                        </div>

                                        <button
                                            type="button"
                                            onClick={() => removeSoftTimeSlotRow(slot.id)}
                                            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
                                            aria-label="Remove timeslot"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="mt-6 flex justify-end">
                            {softTimeSlots.length > 0 && (
                                <button className="mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {handleSaveConstraintsTimeSlots()}}
                                >
                                    Save
                                </button>
                            )}

                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={addSoftTimeSlotRow}
                            >
                                Add timeslot
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Session type ranking</h2>
                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hasAddedRanking ? "Save or delete ranking?" : "No session ranking added yet, add session preference between lab, grading, exercise and help session types?"}
                        </p>

                        { hasAddedRanking && (
                            <SessionTypeRanker
                                value={ranking}
                                onChange={(nextRanking) => {
                                    const prefs = rankingToPreferences(nextRanking);
                                    setForm((prev) => ({
                                        ...prev,
                                        ...prefs,
                                    }));
                                }}
                            />
                        )}

                        <div className="mt-6 flex justify-end">
                            {! hasAddedRanking && (
                                <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {setHasAddedRanking(true)}}
                                >
                                    Add ranking
                                </button>
                            )}

                            {hasAddedRanking && (
                                <button className="mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {setHasAddedRanking(false)}}
                                >
                                    Delete
                                </button>

                            )}
                            {hasAddedRanking && (
                                <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {handleSaveConstraintsNotTimeslots()}}
                                >
                                    Save
                                </button>
                            )}

                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">Compact or spread out schedule</h2>
                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hasAddedSchedulePreference ? "Save or delete schedule?" :
                                "No choice selected yet, add preference for a compact schedule with more sessions during a single day or a spread out schedule over the week?"}
                            </p>

                        {hasAddedSchedulePreference && (
                        <div className="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-3">
                            <div>
                                <p className="text-sm font-medium text-slate-900">
                                    {form.isCompactSchedule ? "Compact schedule" : "Spread out schedule"}
                                </p>
                                <p className="text-xs text-slate-500">
                                    Toggle how tightly you prefer sessions scheduled.
                                </p>
                            </div>

                            <button
                                type="button"
                                onClick={() =>
                                    setForm((prev) => ({
                                        ...prev,
                                        isCompactSchedule: !prev.isCompactSchedule,
                                    }))
                                }
                                className={[
                                    "relative inline-flex h-8 w-14 items-center rounded-full transition",
                                    form.isCompactSchedule ? "bg-[#003b5c]" : "bg-slate-300",
                                ].join(" ")}
                                aria-pressed={form.isCompactSchedule}
                            >
                                        <span
                                            className={[
                                                "inline-block h-6 w-6 transform rounded-full bg-white transition",
                                                form.isCompactSchedule ? "translate-x-7" : "translate-x-1",
                                            ].join(" ")}
                                        />
                            </button>
                        </div>
                        )}

                        <div className="mt-6 flex justify-end">
                        {!hasAddedSchedulePreference && (
                            <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {setHasAddedSchedulePreference(true)}}
                            >
                                Add selection
                            </button>
                        )}

                        {hasAddedSchedulePreference && (
                                <button className="mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {setHasAddedSchedulePreference(false)}}
                                >
                                    Delete
                                </button>
                        )}

                        {hasAddedSchedulePreference && (
                                <button className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                        onClick={() => {handleSaveConstraintsNotTimeslots()}}
                                >
                                    Save
                                </button>
                        )}
                        </div>

                    </section>
                </section>
            </main>
        </div>
    )
}