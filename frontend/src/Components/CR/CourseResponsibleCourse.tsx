import SideTabNav from "../SideTabNav.tsx";
import { FolderOpen, Trash, Plus, CalendarDays, UserCircle2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../AuthContext.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import {
    getCourseById,
    archiveCourse,
    deleteCourse,
    type CourseResponse,
} from "../../api/coursesApi.ts";
import {
    getCourseSessions,
    createCourseSession,
    deleteCourseSession,
    type CourseSessionResponse,
    type CourseSessionType,
    type CreateCourseSessionRequest,
} from "../../api/courseSessionsApi.ts";

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

export function CourseResponsibleCourse() {
    const { accessToken, user } = useAuth();
    const { currentCourseId, setCurrentCourseId } = useCurrentCourse();

    const [course, setCourse] = useState<CourseResponse | null>(null);
    const [sessions, setSessions] = useState<CourseSessionResponse[]>([]);

    const [isLoadingCourse, setIsLoadingCourse] = useState(false);
    const [isLoadingSessions, setIsLoadingSessions] = useState(false);

    const [courseError, setCourseError] = useState<string | null>(null);
    const [sessionsError, setSessionsError] = useState<string | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);

    const [isArchiving, setIsArchiving] = useState(false);
    const [isDeletingCourse, setIsDeletingCourse] = useState(false);
    const [isCreatingSession, setIsCreatingSession] = useState(false);

    const [form, setForm] = useState<CreateCourseSessionRequest>({
        startDateTime: "",
        endDateTime: "",
        courseSessionType: "LABORATION",
        minTAs: 1,
        maxTAs: 1, isWeeklyRecurring: false,
    });

    const sortedSessions = useMemo(() => {
        return [...sessions].sort(
            (a, b) =>
                new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime()
        );
    }, [sessions]);

    const loadCourse = async () => {
        if (!currentCourseId || !accessToken) return;

        setIsLoadingCourse(true);
        setCourseError(null);

        try {
            const fetchedCourse = await getCourseById(currentCourseId, accessToken);
            setCourse(fetchedCourse);
        } catch (error) {
            console.error(error);
            setCourseError("Could not load course.");
            setCourse(null);
        } finally {
            setIsLoadingCourse(false);
        }
    };

    const loadSessions = async () => {
        if (!currentCourseId || !accessToken) return;

        setIsLoadingSessions(true);
        setSessionsError(null);

        try {
            const fetchedSessions = await getCourseSessions(currentCourseId, accessToken);
            setSessions(fetchedSessions);
        } catch (error) {
            console.error(error);
            setSessionsError("Could not load course sessions.");
            setSessions([]);
        } finally {
            setIsLoadingSessions(false);
        }
    };

    useEffect(() => {
        if (!currentCourseId) {
            setCourse(null);
            setSessions([]);
            return;
        }

        loadCourse();
        loadSessions();
    }, [currentCourseId, accessToken]);

    const isOwner = !!course && !!user && course.owner.email === user.email;
    const isCourseResponsible = user?.userType === "CR"

    const handleCreateSession = async () => {
        if (!currentCourseId || !accessToken) return;
        if (!form.startDateTime || !form.endDateTime) return;

        setIsCreatingSession(true);
        setActionError(null);

        try {
            await createCourseSession(currentCourseId, form, accessToken);
            await loadSessions();

            setForm({
                startDateTime: "",
                endDateTime: "",
                courseSessionType: "LABORATION",
                minTAs: 1,
                maxTAs: 1,
                isWeeklyRecurring: false,
            });
        } catch (error) {
            console.error(error);
            setActionError("Could not create course session.");
        } finally {
            setIsCreatingSession(false);
        }
    };

    const handleDeleteSession = async (courseSessionId: string) => {
        if (!currentCourseId || !accessToken) return;

        setActionError(null);

        try {
            await deleteCourseSession(currentCourseId, courseSessionId, accessToken);
            await loadSessions();
        } catch (error) {
            console.error(error);
            setActionError("Could not delete course session.");
        }
    };

    const handleArchiveCourse = async () => {
        if (!currentCourseId || !accessToken) return;

        setIsArchiving(true);
        setActionError(null);

        try {
            await archiveCourse(currentCourseId, accessToken);
            await loadCourse();
        } catch (error) {
            console.error(error);
            setActionError("Could not archive course.");
        } finally {
            setIsArchiving(false);
        }
    };

    const handleDeleteCourse = async () => {
        if (!currentCourseId || !accessToken) return;

        setIsDeletingCourse(true);
        setActionError(null);

        try {
            await deleteCourse(currentCourseId, accessToken);
            setCurrentCourseId(null);
            setCourse(null);
            setSessions([]);
        } catch (error) {
            console.error(error);
            setActionError("Could not delete course.");
        } finally {
            setIsDeletingCourse(false);

        }
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="pl-[104px] pt-6 pb-8">
                <div className="mx-auto w-full max-w-6xl px-6 space-y-6">
                    {!currentCourseId && (
                        <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                            <p className="text-slate-600">No course selected.</p>
                        </section>
                    )}


                    {courseError && (
                        <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                            <p className="text-red-600">{courseError}</p>
                        </section>
                    )}

                    {currentCourseId && isLoadingCourse && (
                        <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                            <p className="text-slate-600">Loading course...</p>
                        </section>
                    )}

                    {course && (
                        <section className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                            <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
                                <div className="space-y-4">
                                    <div>
                                        <p className="text-sm font-medium text-slate-500">
                                            Current course
                                        </p>
                                        <h1 className="mt-1 text-3xl font-bold text-slate-900">
                                            {course.courseCode}
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
                                                {course.owner.name}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {isOwner && (
                                    <div className="flex flex-col gap-3 sm:flex-row lg:flex-col">
                                        {course.status == "ARCHIVED" ? (

                                            <button
                                                className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#002741] disabled:opacity-50"
                                                type="button"
                                            >
                                                <FolderOpen className="h-4 w-4" />
                                                {"Archived Course"}
                                            </button>
                                        ) : (
                                            <button
                                                className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#002741] disabled:opacity-50"
                                                type="button"
                                                onClick={handleArchiveCourse}
                                                disabled={isArchiving}
                                            >
                                                <FolderOpen className="h-4 w-4" />
                                                {isArchiving ? "Archiving..." : "Archive Course"}
                                            </button>
                                        )}
                                        <button
                                            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-300 disabled:opacity-50"
                                            type="button"
                                            onClick={handleDeleteCourse}
                                            disabled={isDeletingCourse}
                                        >
                                            <Trash className="h-4 w-4" />
                                            {isDeletingCourse ? "Deleting..." : "Delete Course"}
                                        </button>
                                    </div>
                                )}
                            </div>
                        </section>
                    )}

                    {course && (
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

                            {actionError && (
                                <p className="mt-4 text-sm text-red-600">{actionError}</p>
                            )}

                            {isCourseResponsible && (
                                <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                    <h3 className="text-sm font-semibold text-slate-900">
                                        Create course session
                                    </h3>

                                    <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-6">
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
                                                Min TAs
                                            </label>
                                            <input
                                                type="number"
                                                min={1}
                                                value={form.minTAs}
                                                onChange={(e) =>
                                                    setForm((prev) => ({
                                                        ...prev,
                                                        minTAs: Number(e.target.value),
                                                    }))
                                                }
                                                className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                            />
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
                                                disabled={isCreatingSession}
                                                className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#002741] disabled:opacity-50"
                                            >
                                                <Plus className="h-4 w-4" />
                                                {isCreatingSession ? "Creating..." : "Create"}
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {isLoadingSessions ? (
                                <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 text-slate-600">
                                    Loading course sessions...
                                </div>
                            ) : sessionsError ? (
                                <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 text-red-600">
                                    {sessionsError}
                                </div>
                            ) : (
                                <div className="mt-6 overflow-hidden rounded-2xl border border-slate-200">
                                    <div className="hidden grid-cols-12 bg-slate-100 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500 md:grid">
                                        <div className="col-span-3">Start</div>
                                        <div className="col-span-3">End</div>
                                        <div className="col-span-2">Type</div>
                                        <div className="col-span-2">Min / Max TAs</div>
                                        <div className="col-span-2">Actions</div>
                                    </div>

                                    <div className="divide-y divide-slate-200 bg-white">
                                        {sortedSessions.map((session) => (
                                            <div
                                                key={session.courseSessionId}
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
                                                        Min / Max TAs
                                                    </p>
                                                    <p className="text-sm text-slate-700">
                                                        {session.minTAs} / {session.maxTAs}
                                                    </p>
                                                    {session.isWeeklyRecurring && (
                                                        <p className="mt-1 text-xs text-slate-500">Weekly recurring</p>
                                                    )}
                                                </div>

                                                <div className="md:col-span-2">
                                                    {isOwner ? (
                                                        <button
                                                            type="button"
                                                            onClick={() => handleDeleteSession(session.courseSessionId)}
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
                            )}
                        </section>
                    )}
                </div>
            </main>
        </div>
    );
}