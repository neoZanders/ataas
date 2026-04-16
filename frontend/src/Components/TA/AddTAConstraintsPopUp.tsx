import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Plus, X, Trash2, ChevronDown } from "lucide-react";
import SessionTypeRanker, { SessionType } from "./SessionRanking1to4.tsx";
import type { CourseAssignmentConstraintsRequest } from "../../api/taConstraintsApi.ts";

type RankingState = Record<SessionType, number | null>;

type ConstraintKind =
    | "timeSlotsCantWork"
    | "timeSlotsCantWorkSoft"
    | "sessionPreference"
    | "compactSchedule";

export type TimeSlot = {
    id: string;
    date: string;
    startTime: string;
    endTime: string;
    constraintType: "SOFT" | "HARD";
    backendId?: string;
    isWeeklyRecurring: boolean;
};

interface AddTAConstraintsPopUpProps {
    isOpen: boolean;
    onClose: () => void;

    form: CourseAssignmentConstraintsRequest;
    setForm: React.Dispatch<React.SetStateAction<CourseAssignmentConstraintsRequest>>;

    hardTimeSlots: TimeSlot[];
    setHardTimeSlots: React.Dispatch<React.SetStateAction<TimeSlot[]>>;

    softTimeSlots: TimeSlot[];
    setSoftTimeSlots: React.Dispatch<React.SetStateAction<TimeSlot[]>>;

    onSave: () => Promise<void>;
}


const kindLabel: Record<ConstraintKind, string> = {
    timeSlotsCantWork: "Timeslots you can't work",
    timeSlotsCantWorkSoft: "Timeslots you prefer not to work",
    sessionPreference: "Session type preference",
    compactSchedule: "Compact or spread out schedule",
};

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

export function AddTAConstraintsPopUp({
                                          isOpen,
                                          onClose,
                                          form,
                                          setForm,
                                          hardTimeSlots,
                                          setHardTimeSlots,
                                          softTimeSlots,
                                          setSoftTimeSlots,
                                          onSave,
                                      }: AddTAConstraintsPopUpProps) {
    const [enabled, setEnabled] = useState<Record<ConstraintKind, boolean>>({
        timeSlotsCantWork: false,
        timeSlotsCantWorkSoft: false,
        sessionPreference: false,
        compactSchedule: false,
    });

    const [isSaving, setIsSaving] = useState(false);

    const ranking = useMemo(() => preferencesToRanking(form), [form]);

    const availableKinds = useMemo(() => {
        const allKinds: ConstraintKind[] = [
            "timeSlotsCantWork",
            "timeSlotsCantWorkSoft",
            "sessionPreference",
            "compactSchedule",
        ];
        return allKinds.filter((k) => !enabled[k]);
    }, [enabled]);

    const handleClose = useCallback(() => {
        onClose();
    }, [onClose]);

    useEffect(() => {
        if (!isOpen) return;

        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") handleClose();
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isOpen, handleClose]);

    useEffect(() => {
        if (!isOpen) return;

        setEnabled({
            timeSlotsCantWork: hardTimeSlots.length > 0,
            timeSlotsCantWorkSoft: softTimeSlots.length > 0,
            sessionPreference: true,
            compactSchedule: true,
        });
    }, [isOpen, hardTimeSlots.length, softTimeSlots.length]);

    if (!isOpen) return null;

    const addConstraint = (kind: ConstraintKind) => {
        setEnabled((prev) => ({ ...prev, [kind]: true }));
        
    };

    const removeConstraint = (kind: ConstraintKind) => {
        setEnabled((prev) => ({ ...prev, [kind]: false }));

        if (kind === "timeSlotsCantWork") {
            setHardTimeSlots([]);
        }

        if (kind === "timeSlotsCantWorkSoft") {
            setSoftTimeSlots([]);
        }

        if (kind === "compactSchedule") {
            setForm((prev) => ({
                ...prev,
                isCompactSchedule: false,
            }));
        }

        if (kind === "sessionPreference") {
            setForm((prev) => ({
                ...prev,
                sessionTypePreference1: "LABORATION",
                sessionTypePreference2: "HELP",
                sessionTypePreference3: "EXERCISE",
                sessionTypePreference4: "GRADING",
            }));
        }
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

    const handleSave = async () => {
        setIsSaving(true);
        try {
            await onSave();
            handleClose();
        } catch (error) {
            console.error("Failed to save popup constraints", error);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/20 px-4 backdrop-blur-sm"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) handleClose();
            }}
        >
            <div className="relative w-full max-w-6xl rounded-3xl bg-white shadow-xl ring-1 ring-slate-200 max-h-[85vh] flex flex-col">
                <button
                    type="button"
                    onClick={handleClose}
                    className="absolute right-4 top-4 z-10 inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
                    aria-label="Close popup"
                >
                    <X className="h-4 w-4" />
                </button>

                <div className="px-6 pt-6 pb-4 overflow-y-auto">
                    <div className="mb-5 pr-12">
                        <h2 className="text-xl font-semibold text-slate-900">Add TA constraints</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Add one or more constraints. You can remove a constraint at any time.
                        </p>
                    </div>

                    <div className="mb-5">
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Add constraint
                        </label>

                        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                            <div className="relative w-full">
                                <select
                                    className="w-full appearance-none rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                    defaultValue=""
                                    onChange={(e) => {
                                        const kind = e.target.value as ConstraintKind;
                                        if (!kind) return;
                                        addConstraint(kind);
                                        e.currentTarget.value = "";
                                    }}
                                >
                                    <option value="" disabled>
                                        Select a constraint to add...
                                    </option>
                                    {availableKinds.map((k) => (
                                        <option key={k} value={k}>
                                            {kindLabel[k]}
                                        </option>
                                    ))}
                                </select>

                                <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-4 text-slate-400">
                                    <ChevronDown className="h-4 w-4" />
                                </span>
                            </div>

                            <div className="hidden sm:flex items-center gap-2 text-xs text-slate-500">
                                <Plus className="h-4 w-4" />
                                Add multiple
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {enabled.sessionPreference && (
                            <SectionShell
                                title="Session Preference"
                                onRemove={() => removeConstraint("sessionPreference")}
                                className="md:col-span-2"
                            >
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
                            </SectionShell>
                        )}

                        {enabled.compactSchedule && (
                            <SectionShell
                                title="Compact or spread out schedule?"
                                onRemove={() => removeConstraint("compactSchedule")}
                                className="md:col-span-2"
                            >
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
                            </SectionShell>
                        )}

                        {enabled.timeSlotsCantWorkSoft && (
                            <SectionShell
                                title="Timeslots prefer not to work"
                                onRemove={() => removeConstraint("timeSlotsCantWorkSoft")}
                                className="md:col-span-2"
                            >
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

                                    <button
                                        type="button"
                                        onClick={addSoftTimeSlotRow}
                                        className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                                    >
                                        <Plus className="h-4 w-4" />
                                        Add timeslot
                                    </button>
                                </div>
                            </SectionShell>
                        )}

                        {enabled.timeSlotsCantWork && (
                            <SectionShell
                                title="Timeslots can't work"
                                onRemove={() => removeConstraint("timeSlotsCantWork")}
                                className="md:col-span-2"
                            >
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

                                    <button
                                        type="button"
                                        onClick={addHardTimeSlotRow}
                                        className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                                    >
                                        <Plus className="h-4 w-4" />
                                        Add timeslot
                                    </button>
                                </div>
                            </SectionShell>
                        )}
                    </div>
                </div>

                <div className="px-6 pb-6 pt-4 border-t border-slate-200 bg-white rounded-b-3xl">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
                        <button
                            type="button"
                            onClick={handleClose}
                            className="inline-flex items-center justify-center rounded-2xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                        >
                            Cancel
                        </button>

                        <button
                            type="button"
                            onClick={handleSave}
                            disabled={isSaving}
                            className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:opacity-50"
                        >
                            {isSaving ? "Saving..." : "Save constraints"}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}