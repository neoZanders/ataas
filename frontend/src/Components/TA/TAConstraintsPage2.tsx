import SideTabNav from "../SideTabNav.tsx";
import { useAuth } from "../AuthContext.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import { useEffect, useMemo, useState } from "react";
import { type CourseResponse, getCourseById } from "../../api/coursesApi.ts";
import {
    type CourseAssignmentConstraintsRequest,
    type CourseAssignmentConstraintsResponse,
    createTAConstraintNotASession,
    getCourseAssignmentConstraints,
    getTAConstraintsTimeSlots,
    putTAConstraintsTimeSlots,
    type PutTAConstraintsTimeSlotsRequest,
    type TAConstraintsTimeSlotsResponse,
} from "../../api/taConstraintsApi.ts";
import { Trash2 } from "lucide-react";
import SessionTypeRanker, { SessionType } from "./SessionRanking1to4.tsx";
import type { CourseSessionType } from "../../api/courseSessionsApi.ts";

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

type RankingState = Record<SessionType, number | null>;

type SaveSection =
    | "hours"
    | "hardTimeslots"
    | "softTimeslots"
    | "ranking"
    | "schedule"
    | null;

export function TAConstraintsPage2() {
    const { accessToken, user } = useAuth();
    const { currentCourseId } = useCurrentCourse();

    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [savingSection, setSavingSection] = useState<SaveSection>(null);
    const [successSection, setSuccessSection] = useState<SaveSection>(null);
    const [errorSection, setErrorSection] = useState<SaveSection>(null);
    const [saveError, setSaveError] = useState<string | null>(null);

    const [hardTimeSlots, setHardTimeSlots] = useState<TimeSlot[]>([]);
    const [softTimeSlots, setSoftTimeSlots] = useState<TimeSlot[]>([]);

    const [hasAddedRanking, setHasAddedRanking] = useState<boolean>(false);
    const [hasAddedSchedulePreference, setHasAddedSchedulePreference] = useState<boolean>(false);
    const [hasAddedHardDescription, setHasAddedHardDescription] = useState<boolean>(false);
    const [hasAddedSoftDescription, setHasAddedSoftDescription] = useState<boolean>(false);

    const [form, setForm] = useState<CourseAssignmentConstraintsForm>({
        minHours: "",
        maxHours: "",
        sessionTypePreference1: null,
        sessionTypePreference2: null,
        sessionTypePreference3: null,
        sessionTypePreference4: null,
        isCompactSchedule: null,
    });

    const request: CourseAssignmentConstraintsRequest = {
        minHours: form.minHours === "" ? 0 : Number(form.minHours),
        maxHours: form.maxHours === "" ? 0 : Number(form.maxHours),
        sessionTypePreference1: form.sessionTypePreference1,
        sessionTypePreference2: form.sessionTypePreference2,
        sessionTypePreference3: form.sessionTypePreference3,
        sessionTypePreference4: form.sessionTypePreference4,
        isCompactSchedule: form.isCompactSchedule,
    };

    function clearSaveStatus() {
        setSuccessSection(null);
        setErrorSection(null);
        setSaveError(null);
    }

    function showSaveSuccess(section: SaveSection) {
        setSuccessSection(section);
        setErrorSection(null);
        setSaveError(null);
    }

    function showSaveError(section: SaveSection, message: string) {
        setErrorSection(section);
        setSuccessSection(null);
        setSaveError(message);
    }

    function SaveStatus({ section }: { section: SaveSection }) {
        return (
            <>
                {errorSection === section && saveError && (
                    <p className="mb-4 mt-4 text-sm text-red-600">{saveError}</p>
                )}

                {successSection === section && (
                    <p className="mb-4 mt-4 text-sm text-emerald-600">
                        Constraints saved.
                    </p>
                )}
            </>
        );
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

    function preferencesToRanking(form: CourseAssignmentConstraintsForm): RankingState {
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

    function rankingToPreferences(ranking: RankingState): Pick<
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
            sessionTypePreference1: rankedItems[0]?.type ?? "LABORATION",
            sessionTypePreference2: rankedItems[1]?.type ?? "GRADING",
            sessionTypePreference3: rankedItems[2]?.type ?? "HELP",
            sessionTypePreference4: rankedItems[3]?.type ?? "EXERCISE",
        };
    }

    const ranking = useMemo(() => preferencesToRanking(form), [form]);

    function hasSavedRanking(response: CourseAssignmentConstraintsResponse) {
        return (
            response.sessionTypePreference1 !== null &&
            response.sessionTypePreference2 !== null &&
            response.sessionTypePreference3 !== null &&
            response.sessionTypePreference4 !== null
        );
    }

    function hasSavedSchedulePreference(response: CourseAssignmentConstraintsResponse) {
        return response.isCompactSchedule !== null;
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
            prev.map((slot) => (slot.id === id ? { ...slot, ...updates } : slot))
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
            },
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
            },
        ]);
    };

    const handleSaveConstraintsNotTimeslots = async (
        section: "hours" | "ranking" | "schedule"
    ) => {
        if (!currentCourseId) {
            showSaveError(
                section,
                "No course selected, go to courses and select a course!"
            );
            return false;
        }

        if (!user?.id) {
            showSaveError(section, "No user found.");
            return false;
        }

        if (!accessToken) {
            showSaveError(section, "You are not logged in. Please log in again.");
            return false;
        }

        setSavingSection(section);
        clearSaveStatus();

        try {
            const response = await createTAConstraintNotASession(
                request,
                currentCourseId,
                user.id,
                accessToken
            );

            setForm({
                minHours: String(response.minHours ?? ""),
                maxHours: String(response.maxHours ?? ""),
                sessionTypePreference1: response.sessionTypePreference1,
                sessionTypePreference2: response.sessionTypePreference2,
                sessionTypePreference3: response.sessionTypePreference3,
                sessionTypePreference4: response.sessionTypePreference4,
                isCompactSchedule: response.isCompactSchedule,
            });

            setHasAddedSchedulePreference(hasSavedSchedulePreference(response));
            setHasAddedRanking(hasSavedRanking(response));

            showSaveSuccess(section);
            return true;
        } catch (error) {
            console.error("Failed to save constraints", error);
            showSaveError(section, "Could not save constraints.");
            return false;
        } finally {
            setSavingSection(null);
        }
    };

    const handleSaveRanking = async () => {
        const success = await handleSaveConstraintsNotTimeslots("ranking");

        if (success) {
            setHasAddedRanking(true);
        }
    };

    const handleSaveSchedulePreference = async () => {
        const success = await handleSaveConstraintsNotTimeslots("schedule");

        if (success) {
            setHasAddedSchedulePreference(true);
        }
    };

    const handleSaveConstraintsTimeSlots = async (
        section: "hardTimeslots" | "softTimeslots"
    ) => {
        if (!currentCourseId) {
            showSaveError(
                section,
                "No course selected, go to courses and select a course!"
            );
            return;
        }

        if (!accessToken) {
            showSaveError(section, "You are not logged in. Please log in again.");
            return;
        }

        setSavingSection(section);
        clearSaveStatus();

        try {
            const timeSlotRequest = mapTimeSlotsToPutRequest(
                hardTimeSlots,
                softTimeSlots
            );

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

            showSaveSuccess(section);
        } catch (error) {
            console.error("Failed to save constraints", error);
            showSaveError(section, "Could not save constraints.");
        } finally {
            setSavingSection(null);
        }
    };

    useEffect(() => {
        const loadCourse = async () => {
            if (!currentCourseId || !accessToken) {
                setCourse(null);
                return;
            }

            try {
                const fetchedCourse = await getCourseById(
                    currentCourseId,
                    accessToken
                );
                setCourse(fetchedCourse);
            } catch (error) {
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
                    minHours: String(response.minHours ?? ""),
                    maxHours: String(response.maxHours ?? ""),
                    sessionTypePreference1: response.sessionTypePreference1,
                    sessionTypePreference2: response.sessionTypePreference2,
                    sessionTypePreference3: response.sessionTypePreference3,
                    sessionTypePreference4: response.sessionTypePreference4,
                    isCompactSchedule: response.isCompactSchedule,
                });

                setHasAddedSchedulePreference(
                    hasSavedSchedulePreference(response)
                );
                setHasAddedRanking(hasSavedRanking(response));
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
                <h1 className="text-3xl text-center font-bold text-slate-900">
                    TA Constraints {course?.courseCode}
                </h1>

                <section className="rounded-3xl bg-white p-6 mt-2 shadow-sm ring-1 ring-slate-200">
                    <h1 className="text-xl text-center font-bold text-slate-900">
                        Input your hard constraints below:
                    </h1>


                    <button
                        className="cursor-pointer mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center underline hover:text-slate-700"
                        onClick={() => {setHasAddedHardDescription(true)}}
                    >
                        Description of hard constraints usage
                    </button>

                    {hasAddedHardDescription && (
                        <p className="mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center ">
                            The algorithm will guarantee fulfilling hard constraints.
                            You will never have to work more than your max hours or less than your min hours and you can't be scheduled during your specified timeslots.
                        </p>
                    )}

                    <section className="rounded-3xl bg-red-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">
                            Total course working hours (Mandatory)
                        </h2>

                        <p className="mt-1 text-sm text-slate-500">
                            Set your preferred minimum and maximum number of working hours.
                        </p>

                        <SaveStatus section="hours" />

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
                                                minHours: e.target.value,
                                            }))
                                        }
                                        className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                    />

                                    <span className="text-sm text-slate-600">
                                        hours
                                    </span>
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
                                                maxHours: e.target.value,
                                            }))
                                        }
                                        className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                    />

                                    <span className="text-sm text-slate-600">
                                        hours
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                type="button"
                                onClick={() =>
                                    handleSaveConstraintsNotTimeslots("hours")
                                }
                                disabled={savingSection === "hours"}
                                className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                            >
                                {savingSection === "hours"
                                    ? "Saving..."
                                    : "Save hours"}
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">
                            Timeslots you can't work
                        </h2>

                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hardTimeSlots.length > 0
                                ? "Add more timeslots or save?"
                                : "No timeslots added yet, add timeslots?"}
                        </p>

                        <SaveStatus section="hardTimeslots" />

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
                                                        updateHardTimeSlot(slot.id, {
                                                            date: e.target.value,
                                                        })
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
                                                        updateHardTimeSlot(slot.id, {
                                                            startTime: e.target.value,
                                                        })
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
                                                        updateHardTimeSlot(slot.id, {
                                                            endTime: e.target.value,
                                                        })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div className="flex items-center pt-6">
                                                <label className="inline-flex items-center gap-2 text-sm text-slate-700">
                                                    <input
                                                        type="checkbox"
                                                        checked={
                                                            slot.isWeeklyRecurring
                                                        }
                                                        onChange={(e) =>
                                                            updateHardTimeSlot(slot.id, {
                                                                isWeeklyRecurring:
                                                                e.target.checked,
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
                                            onClick={() =>
                                                removeHardTimeSlotRow(slot.id)
                                            }
                                            className="cursor-pointer inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
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
                                <button
                                    className="cursor-pointer mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() =>
                                        handleSaveConstraintsTimeSlots(
                                            "hardTimeslots"
                                        )
                                    }
                                    disabled={savingSection === "hardTimeslots"}
                                >
                                    {savingSection === "hardTimeslots"
                                        ? "Saving..."
                                        : "Save"}
                                </button>
                            )}

                            <button
                                className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                onClick={addHardTimeSlotRow}
                            >
                                Add timeslot
                            </button>
                        </div>
                    </section>
                </section>

                <section className="rounded-3xl bg-white p-6 mt-2 shadow-sm ring-1 ring-slate-200">
                    <h1 className="text-xl text-center font-bold text-slate-900">
                        Input your soft constraints below:
                    </h1>

                    <button
                        className="cursor-pointer mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center underline hover:text-slate-700"
                        onClick={() => {setHasAddedSoftDescription(true)}}
                    >
                        Description of soft constraints usage
                    </button>

                    {hasAddedSoftDescription && (
                        <p className="mt-1 text-sm mb-4 text-slate-500 w-full flex items-center justify-center ">
                            The algorithm will try to fulfilling soft constraints.
                        </p>
                    )}

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">
                            Timeslots you prefer not to work
                        </h2>

                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {softTimeSlots.length > 0
                                ? "Add timeslot or save timeslots?"
                                : "No timeslots added yet, add timeslots?"}
                        </p>

                        <SaveStatus section="softTimeslots" />

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
                                                        updateSoftTimeSlot(slot.id, {
                                                            date: e.target.value,
                                                        })
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
                                                        updateSoftTimeSlot(slot.id, {
                                                            startTime: e.target.value,
                                                        })
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
                                                        updateSoftTimeSlot(slot.id, {
                                                            endTime: e.target.value,
                                                        })
                                                    }
                                                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                                />
                                            </div>

                                            <div className="flex items-center pt-6">
                                                <label className="inline-flex items-center gap-2 text-sm text-slate-700">
                                                    <input
                                                        type="checkbox"
                                                        checked={
                                                            slot.isWeeklyRecurring
                                                        }
                                                        onChange={(e) =>
                                                            updateSoftTimeSlot(slot.id, {
                                                                isWeeklyRecurring:
                                                                e.target.checked,
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
                                            onClick={() =>
                                                removeSoftTimeSlotRow(slot.id)
                                            }
                                            className="cursor-pointer inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
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
                                <button
                                    className="cursor-pointer mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() =>
                                        handleSaveConstraintsTimeSlots(
                                            "softTimeslots"
                                        )
                                    }
                                    disabled={savingSection === "softTimeslots"}
                                >
                                    {savingSection === "softTimeslots"
                                        ? "Saving..."
                                        : "Save"}
                                </button>
                            )}

                            <button
                                className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                onClick={addSoftTimeSlotRow}
                            >
                                Add timeslot
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">
                            Session type ranking
                        </h2>

                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hasAddedRanking
                                ? "Save or delete ranking?"
                                : "No session ranking added yet, add session preference between lab, grading, exercise and help session types?"}
                        </p>

                        <SaveStatus section="ranking" />

                        {hasAddedRanking && (
                            <SessionTypeRanker
                                value={ranking}
                                onChange={(nextRanking) => {
                                    const prefs =
                                        rankingToPreferences(nextRanking);

                                    setForm((prev) => ({
                                        ...prev,
                                        ...prefs,
                                    }));
                                }}
                            />
                        )}

                        <div className="mt-6 flex justify-end">
                            {!hasAddedRanking && (
                                <button
                                    className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {
                                        setHasAddedRanking(true);

                                        setForm((prev) => ({
                                            ...prev,
                                            sessionTypePreference1:
                                                prev.sessionTypePreference1 ??
                                                "LABORATION",
                                            sessionTypePreference2:
                                                prev.sessionTypePreference2 ??
                                                "GRADING",
                                            sessionTypePreference3:
                                                prev.sessionTypePreference3 ??
                                                "HELP",
                                            sessionTypePreference4:
                                                prev.sessionTypePreference4 ??
                                                "EXERCISE",
                                        }));
                                    }}
                                >
                                    Add ranking
                                </button>
                            )}

                            {hasAddedRanking && (
                                <button
                                    className="cursor-pointer mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {
                                        setHasAddedRanking(false);

                                        setForm((prev) => ({
                                            ...prev,
                                            sessionTypePreference1: null,
                                            sessionTypePreference2: null,
                                            sessionTypePreference3: null,
                                            sessionTypePreference4: null,
                                        }));
                                    }}
                                >
                                    Delete
                                </button>
                            )}

                            {hasAddedRanking && (
                                <button
                                    className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={handleSaveRanking}
                                    disabled={savingSection === "ranking"}
                                >
                                    {savingSection === "ranking"
                                        ? "Saving..."
                                        : "Save"}
                                </button>
                            )}
                        </div>
                    </section>

                    <section className="rounded-3xl mt-4 bg-sky-50 p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="mt-1 text-lg font-semibold text-slate-900">
                            Compact or spread out schedule
                        </h2>

                        <p className="mt-1 mb-4 text-sm text-slate-500">
                            {hasAddedSchedulePreference
                                ? "Save or delete schedule?"
                                : "No choice selected yet, add preference for a compact schedule with more sessions during a single day or a spread out schedule over the week?"}
                        </p>

                        <SaveStatus section="schedule" />

                        {hasAddedSchedulePreference && (
                            <div className="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-3">
                                <div>
                                    <p className="text-sm font-medium text-slate-900">
                                        {(form.isCompactSchedule ?? false)
                                            ? "Compact schedule"
                                            : "Spread out schedule"}
                                    </p>

                                    <p className="text-xs text-slate-500">
                                        Toggle how tightly you prefer sessions
                                        scheduled.
                                    </p>
                                </div>

                                <button
                                    type="button"
                                    onClick={() =>
                                        setForm((prev) => ({
                                            ...prev,
                                            isCompactSchedule: !(
                                                prev.isCompactSchedule ?? false
                                            ),
                                        }))
                                    }
                                    className={[
                                        "cursor-pointer relative inline-flex h-8 w-14 items-center rounded-full transition",
                                        (form.isCompactSchedule ?? false)
                                            ? "bg-[#003b5c]"
                                            : "bg-slate-300",
                                    ].join(" ")}
                                    aria-pressed={form.isCompactSchedule ?? false}
                                >
                                    <span
                                        className={[
                                            "inline-block h-6 w-6 transform rounded-full bg-white transition",
                                            (form.isCompactSchedule ?? false)
                                                ? "translate-x-7"
                                                : "translate-x-1",
                                        ].join(" ")}
                                    />
                                </button>
                            </div>
                        )}

                        <div className="mt-6 flex justify-end">
                            {!hasAddedSchedulePreference && (
                                <button
                                    className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {
                                        setHasAddedSchedulePreference(true);

                                        setForm((prev) => ({
                                            ...prev,
                                            isCompactSchedule:
                                                prev.isCompactSchedule ?? false,
                                        }));
                                    }}
                                >
                                    Add selection
                                </button>
                            )}

                            {hasAddedSchedulePreference && (
                                <button
                                    className="cursor-pointer mr-4 inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={() => {
                                        setHasAddedSchedulePreference(false);

                                        setForm((prev) => ({
                                            ...prev,
                                            isCompactSchedule: null,
                                        }));
                                    }}
                                >
                                    Delete
                                </button>
                            )}

                            {hasAddedSchedulePreference && (
                                <button
                                    className="cursor-pointer inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                                    onClick={handleSaveSchedulePreference}
                                    disabled={savingSection === "schedule"}
                                >
                                    {savingSection === "schedule"
                                        ? "Saving..."
                                        : "Save"}
                                </button>
                            )}
                        </div>
                    </section>
                </section>
            </main>
        </div>
    );
}