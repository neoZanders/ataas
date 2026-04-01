import SideTabNav from "../SideTabNav.tsx";
import { FolderOpen, Trash, Plus, CalendarDays, UserCircle2 } from "lucide-react";
import { useMemo, useState } from "react";

type CourseSessionType = "GRADING" | "LABORATION" | "HELP" | "EXERCISE";

type Course = {
    id: string;
    courseCode: string;
    courseName: string;
    description: string;
    startDate: string; // YYYY-MM-DD
    endDate: string;   // YYYY-MM-DD
    ownerName: string;
    isCurrentUserOwner: boolean;
};

type CourseSession = {
    id: string;
    startDateTime: string; // ISO-ish for now
    endDateTime: string;
    courseSessionType: CourseSessionType;
    maxTAs: number;
    isWeeklyRecurring: boolean;
};

type CreateCourseSessionRequest = {
    startDateTime: string;
    endDateTime: string;
    courseSessionType: CourseSessionType;
    maxTAs: number;
    isWeeklyRecurring: boolean;
};

const mockCourse: Course = {
    id: "1",
    courseCode: "DAT216",
    courseName: "Software Engineering Project",
    description:
        "A project-based course focusing on teamwork, system design, implementation, and delivery.",
    startDate: "2026-01-15",
    endDate: "2026-06-05",
    ownerName: "Anna Andersson",
    isCurrentUserOwner: true,
};

const initialSessions: CourseSession[] = [
    {
        id: "1",
        startDateTime: "2026-02-03T10:00",
        endDateTime: "2026-02-03T12:00",
        courseSessionType: "LABORATION",
        maxTAs: 4,
        isWeeklyRecurring: true,
    },
    {
        id: "2",
        startDateTime: "2026-02-05T13:15",
        endDateTime: "2026-02-05T15:00",
        courseSessionType: "HELP",
        maxTAs: 2,
        isWeeklyRecurring: false,
    },
    {
        id: "3",
        startDateTime: "2026-02-10T08:00",
        endDateTime: "2026-02-10T11:00",
        courseSessionType: "GRADING",
        maxTAs: 6,
        isWeeklyRecurring: false,
    },
];

function formatDate(date: string) {
    return new Date(date).toLocaleDateString("sv-SE");
}

function formatDateTime(dateTime: string) {
    return new Date(dateTime).toLocaleString("sv-SE", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}

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

function sessionTypeBadgeClass(type: CourseSessionType) {
    switch (type) {
        case "GRADING":
            return "bg-violet-100 text-violet-700";
        case "LABORATION":
            return "bg-blue-100 text-blue-700";
        case "HELP":
            return "bg-emerald-100 text-emerald-700";
        case "EXERCISE":
            return "bg-amber-100 text-amber-700";
        default:
            return "bg-slate-100 text-slate-700";
    }
}

function uid() {
    return Math.random().toString(36).slice(2, 10);
}

export function CourseResponsibleCourse() {
    const [course] = useState<Course>(mockCourse);
    const [sessions, setSessions] = useState<CourseSession[]>(initialSessions);

    const [form, setForm] = useState<CreateCourseSessionRequest>({
        startDateTime: "",
        endDateTime: "",
        courseSessionType: "LABORATION",
        maxTAs: 1,
        isWeeklyRecurring: false,
    });

    const sortedSessions = useMemo(() => {
        return [...sessions].sort(
            (a, b) =>
                new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime()
        );
    }, [sessions]);

    const handleCreateSession = () => {
        if (!form.startDateTime || !form.endDateTime) return;

        const newSession: CourseSession = {
            id: uid(),
            ...form,
        };

        setSessions((prev) => [...prev, newSession]);

        setForm({
            startDateTime: "",
            endDateTime: "",
            courseSessionType: "LABORATION",
            maxTAs: 1,
            isWeeklyRecurring: false,
        });
    };

    const handleDeleteSession = (sessionId: string) => {
        setSessions((prev) => prev.filter((session) => session.id !== sessionId));
    };

    const handleArchiveCourse = () => {
        // TODO hook up to backend later
        console.log("Archive course", course.id);
    };

    const handleDeleteCourse = () => {
        // TODO hook up to backend later
        console.log("Delete course", course.id);
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="pl-[104px] pt-6 pb-8">
                <div className="mx-auto w-full max-w-6xl px-6 space-y-6">
                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
                            <div className="space-y-4">
                                <div>
                                    <p className="text-sm font-medium text-slate-500">
                                        Current course
                                    </p>
                                    <h1 className="mt-1 text-3xl font-bold text-slate-900">
                                        {course.courseCode} — {course.courseName}
                                    </h1>
                                </div>

                                <p className="max-w-3xl text-sm leading-6 text-slate-600">
                                    {course.description}
                                </p>

                                <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                                            Start date
                                        </p>
                                        <p className="mt-1 text-sm font-semibold text-slate-900">
                                            {formatDate(course.startDate)}
                                        </p>
                                    </div>

                                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                                            End date
                                        </p>
                                        <p className="mt-1 text-sm font-semibold text-slate-900">
                                            {formatDate(course.endDate)}
                                        </p>
                                    </div>

                                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                                            Owner
                                        </p>
                                        <div className="mt-1 flex items-center gap-2 text-sm font-semibold text-slate-900">
                                            <UserCircle2 className="h-4 w-4 text-slate-500" />
                                            {course.ownerName}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {course.isCurrentUserOwner && (
                                <div className="flex flex-col gap-3 sm:flex-row lg:flex-col">
                                    <button
                                        className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#002741]"
                                        type="button"
                                        onClick={handleArchiveCourse}
                                    >
                                        <FolderOpen className="h-4 w-4" />
                                        Archive Course
                                    </button>

                                    <button
                                        className="inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-300"
                                        type="button"
                                        onClick={handleDeleteCourse}
                                    >
                                        <Trash className="h-4 w-4" />
                                        Delete Course
                                    </button>
                                </div>
                            )}
                        </div>
                    </section>

                    <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="flex items-center gap-2">
                            <CalendarDays className="h-5 w-5 text-[#003b5c]" />
                            <h2 className="text-xl font-semibold text-slate-900">
                                Course Sessions
                            </h2>
                        </div>
                        <p className="mt-1 text-sm text-slate-500">
                            Manage laborations, grading sessions, help sessions, and exercises for
                            this course.
                        </p>

                            <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <h3 className="text-sm font-semibold text-slate-900">
                                    Create course session
                                </h3>

                                <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
                                    <div>
                                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                            Start
                                        </label>
                                        <input
                                            type="datetime-local"
                                            value={form.startDateTime}
                                            onChange={(e) =>
                                                setForm((prev) => ({
                                                    ...prev,
                                                    startDateTime: e.target.value,
                                                }))
                                            }
                                            className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        />
                                    </div>

                                    <div>
                                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                            End
                                        </label>
                                        <input
                                            type="datetime-local"
                                            value={form.endDateTime}
                                            onChange={(e) =>
                                                setForm((prev) => ({
                                                    ...prev,
                                                    endDateTime: e.target.value,
                                                }))
                                            }
                                            className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        />
                                    </div>

                                    <div>
                                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                            Session type
                                        </label>
                                        <select
                                            value={form.courseSessionType}
                                            onChange={(e) =>
                                                setForm((prev) => ({
                                                    ...prev,
                                                    courseSessionType: e.target.value as CourseSessionType,
                                                }))
                                            }
                                            className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        >
                                            <option value="GRADING">Grading</option>
                                            <option value="LABORATION">Laboration</option>
                                            <option value="HELP">Help</option>
                                            <option value="EXERCISE">Exercise</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                            Max TAs
                                        </label>
                                        <input
                                            type="number"
                                            min={1}
                                            value={form.maxTAs}
                                            onChange={(e) =>
                                                setForm((prev) => ({
                                                    ...prev,
                                                    maxTAs: Number(e.target.value),
                                                }))
                                            }
                                            className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        />
                                    </div>

                                    <div className="flex flex-col justify-end">
                                        <label className="mb-3 flex items-center gap-2 text-sm text-slate-700">
                                            <input
                                                type="checkbox"
                                                checked={form.isWeeklyRecurring}
                                                onChange={(e) =>
                                                    setForm((prev) => ({
                                                        ...prev,
                                                        isWeeklyRecurring: e.target.checked,
                                                    }))
                                                }
                                                className="h-4 w-4 rounded border-slate-300 accent-[#003b5c]"
                                            />
                                            Weekly recurring
                                        </label>

                                        <button
                                            type="button"
                                            onClick={handleCreateSession}
                                            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#002741]"
                                        >
                                            <Plus className="h-4 w-4" />
                                            Create
                                        </button>
                                    </div>
                                </div>
                            </div>

                        <div className="mt-6 overflow-hidden rounded-2xl border border-slate-200">
                            <div className="hidden grid-cols-12 bg-slate-100 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500 md:grid">
                                <div className="col-span-3">Start</div>
                                <div className="col-span-3">End</div>
                                <div className="col-span-2">Type</div>
                                <div className="col-span-2">Max TAs</div>
                                <div className="col-span-2">Actions</div>
                            </div>

                            <div className="divide-y divide-slate-200 bg-white">
                                {sortedSessions.map((session) => (
                                    <div
                                        key={session.id}
                                        className="grid grid-cols-1 gap-3 px-4 py-4 md:grid-cols-12 md:items-center"
                                    >
                                        <div className="md:col-span-3">
                                            <p className="text-xs font-medium uppercase tracking-wide text-slate-400 md:hidden">
                                                Start
                                            </p>
                                            <p className="text-sm text-slate-700">
                                                {formatDateTime(session.startDateTime)}
                                            </p>
                                        </div>

                                        <div className="md:col-span-3">
                                            <p className="text-xs font-medium uppercase tracking-wide text-slate-400 md:hidden">
                                                End
                                            </p>
                                            <p className="text-sm text-slate-700">
                                                {formatDateTime(session.endDateTime)}
                                            </p>
                                        </div>

                                        <div className="md:col-span-2">
                                            <p className="text-xs font-medium uppercase tracking-wide text-slate-400 md:hidden">
                                                Type
                                            </p>
                                            <span
                                                className={[
                                                    "inline-flex rounded-full px-2.5 py-1 text-xs font-semibold",
                                                    sessionTypeBadgeClass(session.courseSessionType),
                                                ].join(" ")}
                                            >
                                                {sessionTypeLabel(session.courseSessionType)}
                                            </span>
                                        </div>

                                        <div className="md:col-span-2">
                                            <p className="text-xs font-medium uppercase tracking-wide text-slate-400 md:hidden">
                                                Max TAs
                                            </p>
                                            <p className="text-sm text-slate-700">{session.maxTAs}</p>
                                            {session.isWeeklyRecurring && (
                                                <p className="mt-1 text-xs text-slate-500">Weekly recurring</p>
                                            )}
                                        </div>

                                        <div className="md:col-span-2">
                                            {course.isCurrentUserOwner ? (
                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteSession(session.id)}
                                                    className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
                                                >
                                                    <Trash className="h-4 w-4" />
                                                    Delete
                                                </button>
                                            ) : (
                                                <span className="text-sm text-slate-400">—</span>
                                            )}
                                        </div>
                                    </div>
                                ))}

                                {sortedSessions.length === 0 && (
                                    <div className="px-4 py-8 text-center text-sm text-slate-500">
                                        No course sessions yet.
                                    </div>
                                )}
                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    );
}