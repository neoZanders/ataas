import SideTabNav from "../SideTabNav.tsx";
import { Plus } from "lucide-react";
import {useEffect, useState} from "react";
import {AddTAConstraintsPopUp, type TimeSlot} from "./AddTAConstraintsPopUp.tsx";
import { useAuth } from "../AuthContext.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import {
    type CourseAssignmentConstraintsRequest,
    createTAConstraintNotASession,
    getCourseAssignmentConstraints,
    getTAConstraintsTimeSlots,
    putTAConstraintsTimeSlots, type PutTAConstraintsTimeSlotsRequest, type TAConstraintsTimeSlotsResponse,

} from "../../api/taConstraintsApi.ts";
import {type CourseResponse, getCourseById} from "../../api/coursesApi.ts";

export function TAConstraintsPage() {
    const { accessToken, user } = useAuth();
    const { currentCourseId } = useCurrentCourse();
    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [isPopUpOpen, setIsPopUpOpen] = useState(false);
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
                <div className="mb-6 text-center">
                    <h1 className="text-3xl font-bold text-slate-900">TA Constraints {course?.courseCode}</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Add in your availability and preferences
                    </p>

                    <div className="mt-4 flex justify-center">
                        <button
                            type="button"
                            onClick={() => setIsPopUpOpen(true)}
                            className="inline-flex items-center justify-center gap-2 rounded-full bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                        >
                            <Plus className="h-5 w-5" />
                            Add, Delete or Update Constraints
                        </button>
                    </div>
                </div>

                <div className="mx-auto w-full max-w-7xl px-4 space-y-6">
                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">
                            Weekly working hours
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
                                        type="number"
                                        min={0}
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
                                        type="number"
                                        min={0}
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
                                {isSaving ? "Saving..." : "Save constraints"}
                            </button>
                        </div>
                    </section>

                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">Session type preferences</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Your preferred ranking of different session types.
                        </p>

                        <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                            <ol className="space-y-2 text-sm text-slate-700">
                                <li>
                                    <span className="font-semibold">1.</span> {form.sessionTypePreference1}
                                </li>
                                <li>
                                    <span className="font-semibold">2.</span> {form.sessionTypePreference2}
                                </li>
                                <li>
                                    <span className="font-semibold">3.</span> {form.sessionTypePreference3}
                                </li>
                                <li>
                                    <span className="font-semibold">4.</span> {form.sessionTypePreference4}
                                </li>
                            </ol>
                        </div>
                    </section>

                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">Schedule preference</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Whether you prefer a compact or more spread out schedule.
                        </p>

                        <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                            <p className="text-sm font-medium text-slate-700">
                                {form.isCompactSchedule ? "Compact schedule" : "Spread out schedule"}
                            </p>
                        </div>
                    </section>

                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">Timeslots you can't work</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Hard constraints for times you are unavailable.
                        </p>

                        <div className="mt-5 space-y-3">
                            {hardTimeSlots.length === 0 ? (
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
                                    No hard timeslot constraints added.
                                </div>
                            ) : (
                                hardTimeSlots.map((slot) => (
                                    <div
                                        key={slot.id}
                                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                                    >
                                        <p className="text-sm text-slate-700">
                                            <span className="font-semibold">{slot.date}</span> {slot.startTime}–{slot.endTime}
                                        </p>
                                        {slot.isWeeklyRecurring && (
                                            <p className="mt-1 text-xs text-slate-500">Weekly recurring</p>
                                        )}
                                    </div>
                                ))
                            )}
                        </div>
                    </section>

                    <section className="rounded-3xl bg-white p-6 mb-10 shadow-sm ring-1 ring-slate-200">
                        <h2 className="text-lg font-semibold text-slate-900">Timeslots you prefer not to work</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Soft constraints for less preferred working times.
                        </p>

                        <div className="mt-5 space-y-3">
                            {softTimeSlots.length === 0 ? (
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
                                    No soft timeslot constraints added.
                                </div>
                            ) : (
                                softTimeSlots.map((slot) => (
                                    <div
                                        key={slot.id}
                                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                                    >
                                        <p className="text-sm text-slate-700">
                                            <span className="font-semibold">{slot.date}</span> {slot.startTime}–{slot.endTime}
                                        </p>
                                        {slot.isWeeklyRecurring && (
                                            <p className="mt-1 text-xs text-slate-500">Weekly recurring</p>
                                        )}
                                    </div>
                                ))
                            )}
                        </div>
                    </section>

                </div>
            </main>

            <AddTAConstraintsPopUp
                isOpen={isPopUpOpen}
                onClose={() => setIsPopUpOpen(false)}
                form={form}
                setForm={setForm}
                hardTimeSlots={hardTimeSlots}
                setHardTimeSlots={setHardTimeSlots}
                softTimeSlots={softTimeSlots}
                setSoftTimeSlots={setSoftTimeSlots}
                onSave={handleSaveConstraints}
            />
        </div>
    );
}