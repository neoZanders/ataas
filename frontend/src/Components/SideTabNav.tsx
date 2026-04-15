import { Link, useLocation, matchPath } from "react-router-dom";
import { type ReactNode, useEffect, useMemo, useState } from "react";
import {
    Book,
    Users,
    CalendarDays,
    Megaphone,
    BookCopy,
    UserCircle2,
    Rows3,
} from "lucide-react";
import { useAuth } from "./AuthContext.tsx";
import { CoursesSidePanel } from "./CoursesSidePanel.tsx";
import { CreateCoursePopUp } from "./CreateCoursePopUp.tsx";
import {getCourses, joinCourse} from "../api/coursesApi.ts";
import { useCurrentCourse} from "./CurrentCourseContext.tsx";

type SidebarItem =
    | {
    id: string;
    label: string;
    type: "link";
    to: string;
    pattern?: string;
    icon: ReactNode;
    badgeCount?: number;
}
    | {
    id: string;
    label: string;
    type: "action";
    icon: ReactNode;
    badgeCount?: number;
};

type CoursesSidePanelOption =
    | {
    type: "course";
    label: string;
    courseId: string;
    assignmentStatus: string;
}
    | {
    type: "action";
    label: string;
    action: "create-course" | "join-course";
};


function SideTabNav() {
    const location = useLocation();
    const { user, accessToken } = useAuth();

    const { currentCourseId, setCurrentCourseId } = useCurrentCourse();

    const [coursesOpen, setCoursesOpen] = useState(false);
    const [createCourseOpen, setCreateCourseOpen] = useState(false);
    const [apiCourseOptions, setApiCourseOptions] = useState<CoursesSidePanelOption[]>([]);
    const [isLoadingCourses, setIsLoadingCourses] = useState(false);

    const [joiningCourseId, setJoiningCourseId] = useState<string | null>(null);

    const base =
        user?.userType === "CR" ? "/cr"
            : user?.userType === "TA" ? "/ta"
                : "";

    useEffect(() => {
        if (!currentCourseId || apiCourseOptions.length === 0) return;

        const exists = apiCourseOptions.some(
            (option) => option.type === "course" && option.courseId === currentCourseId
        );

        if (!exists) {
            setCurrentCourseId(null);
        }
    }, [apiCourseOptions, currentCourseId, setCurrentCourseId]);

    useEffect(() => {
        loadCourses();
    }, [accessToken, base]);

    const loadCourses = async () => {
        if (!accessToken) return;

        setIsLoadingCourses(true);

        try {
            const courses = await getCourses(accessToken);

            const mappedCourses: CoursesSidePanelOption[] = courses.map((item) => ({
                type: "course",
                label: item.course.courseCode,
                courseId: item.course.courseId,
                assignmentStatus: item.assignmentStatus,
            }));

            setApiCourseOptions(mappedCourses);
        } catch (error) {
            console.error("Failed to load courses", error);
            setApiCourseOptions([]);
        } finally {
            setIsLoadingCourses(false);
        }
    };

    // eslint-disable-next-line react-hooks/exhaustive-deps
    const staticCourseActionOptions: CoursesSidePanelOption[] =
        user?.userType === "CR"
            ? [{ type: "action", label: "Create course", action: "create-course" }]
            : (user?.userType === "TA")
                ? [{ type: "action", label: "Join course", action: "join-course" }]
                : [];

    const coursePanelOptions = useMemo(
        () => [...apiCourseOptions, ...staticCourseActionOptions],
        [apiCourseOptions, staticCourseActionOptions]
    );

    const items: SidebarItem[] = user?.userType === "CR"
        ? [
            { id: "course", label: "Course", type: "link" , to: `${base}/course`, icon: <Book size={24} /> },
            { id: "courses", label: "Courses", type: "action", icon: <BookCopy size={24} /> },
            { id: "calendar", label: "Calendar", type: "link" , to: `${base}/calendar`, icon: <CalendarDays size={24} /> },
            { id: "ta list", label: "TA list", type: "link" , to: `${base}/talist`, icon: <Users size={24} /> },
            { id: "constraints", label: "Constraints", type: "link", to: `${base}/constraints`, icon: <Rows3 size={24} /> },
            { id: "announcements", label: "Announcements", type: "link" ,  to: `${base}/announcements`, icon: <Megaphone size={24} /> },
        ]
        : user?.userType === "TA"
            ? [
                { id: "course", label: "Course", type: "link" , to: `${base}/course`, icon: <Book size={24} /> },
                { id: "courses", label: "Courses", type: "action", icon: <BookCopy size={24} /> },
                { id: "calendar", label: "Calendar", type: "link" , to: `${base}/calendar`, icon: <CalendarDays size={24} /> },
                { id: "ta list", label: "TA list", type: "link" , to: `${base}/talist`, icon: <Users size={24} /> },
                { id: "constraints", label: "Constraints", type: "link", to: `${base}/constraints`, icon: <Rows3 size={24} /> },
                { id: "announcements", label: "Announcements", type: "link" , to: `${base}/announcements`, icon: <Megaphone size={24} /> },
            ]
            : [];

    const isActive = (item: SidebarItem) =>
        item.type === "link" && !!matchPath({path: item.to, end:false}, location.pathname)

    const isAccountActive = () =>
        location.pathname === "/account" || location.pathname === "/profile";

    const handleJoinCourse = async (courseId: string) => {
        if (!accessToken) return;

        setJoiningCourseId(courseId);

        try {
            await joinCourse(accessToken, courseId);
            await loadCourses();
            setCurrentCourseId(courseId);
        } catch (error) {
            console.error("Failed to join course", error);
        } finally {
            setJoiningCourseId(null);
        }
    };

    return (
        <>
            <aside className="fixed left-0 top-0 h-screen w-[104px] bg-[#003b5c] text-white flex flex-col select-none z-40">
                <div className="pt-2">
                    <Link
                        to="/"
                        className="w-full flex items-center justify-center px-2 py-2"
                        aria-label="Home"
                    >
                        <img
                            src="/SV_Avancez_vit.png"
                            alt="Logo"
                            className="h-[72px] w-[72px] object-contain"
                        />
                    </Link>

                    <Link
                        to="/account"
                        className={[
                            "relative w-full min-h-[78px] px-2 py-2 flex flex-col items-center justify-center gap-1.5 border-b border-white/10 transition",
                            isAccountActive()
                                ? "bg-white text-[#e85d0c]"
                                : "text-white hover:bg-white/5",
                        ].join(" ")}
                        aria-current={isAccountActive() ? "page" : undefined}
                    >
                        {isAccountActive() && (
                            <span className="absolute left-0 top-0 h-full w-[3px] bg-[#e85d0c]" />
                        )}

                        <span className="relative inline-flex items-center justify-center">
                            <UserCircle2 size={30} />
                        </span>
                        <span className="text-[11px] leading-none font-medium text-center">
                            Account
                        </span>
                    </Link>
                </div>

                <nav className="flex flex-col">
                    {items.map((item) => {
                        if (item.type === "action") {
                            const active = coursesOpen;

                            return (
                                <button
                                    key={item.id}
                                    type="button"
                                    onClick={() => setCoursesOpen((prev) => !prev)}
                                    className={[
                                        "relative w-full min-h-[78px] px-2 py-2 flex flex-col items-center justify-center gap-1.5 transition",
                                        active ? "bg-white text-[#e85d0c]" : "text-white hover:bg-white/5",
                                    ].join(" ")}
                                >
                                    {active && (
                                        <span className="absolute left-0 top-0 h-full w-[3px] bg-[#e85d0c]" />
                                    )}

                                    <span className="relative inline-flex items-center justify-center">
                        {item.icon}
                    </span>

                                    <span className="text-[11px] leading-none font-medium text-center">
                        {item.label}
                    </span>
                                </button>
                            );
                        }

                        const active = isActive(item);

                        return (
                            <Link
                                key={item.id}
                                to={item.to}
                                className={[
                                    "relative w-full min-h-[78px] px-2 py-2 flex flex-col items-center justify-center gap-1.5 transition",
                                    active ? "bg-white text-[#e85d0c]" : "text-white hover:bg-white/5",
                                ].join(" ")}
                                aria-current={active ? "page" : undefined}
                            >
                                {active && (
                                    <span className="absolute left-0 top-0 h-full w-[3px] bg-[#e85d0c]" />
                                )}

                                <span className="relative inline-flex items-center justify-center">
                    {item.icon}

                                    {typeof item.badgeCount === "number" && item.badgeCount > 0 && (
                                        <span className="absolute -top-2 -right-4 min-w-[20px] h-5 px-1.5 rounded-full bg-white text-slate-800 border-2 border-[#003b5c] text-[11px] font-bold leading-none flex items-center justify-center">
                            {item.badgeCount}
                        </span>
                                    )}
                </span>

                                <span className="text-[11px] leading-none font-medium text-center">
                    {item.label}
                </span>
                            </Link>
                        );
                    })}
                </nav>
            </aside>

            {coursesOpen && (
                <div className="fixed left-[104px] top-0 h-screen w-64 bg-white border-r border-slate-200 shadow-lg z-50">
                    {isLoadingCourses ? (
                        <div className="p-4 text-sm text-slate-500">Loading courses...</div>
                    ) : (
                        <CoursesSidePanel
                            title="Courses"
                            options={coursePanelOptions}
                            selectedCourseId={currentCourseId}
                            joiningCourseId={joiningCourseId}
                            onSelectCourse={(courseId) => {
                                setCurrentCourseId(courseId);
                                setCoursesOpen(false);
                            }}
                            onJoinCourse={handleJoinCourse}
                            onOpenCreateCourse={() => {
                                setCreateCourseOpen(true);
                            }}
                        />
                    )}
                </div>
            )}

            <CreateCoursePopUp
                isOpen={createCourseOpen}
                onClose={() => setCreateCourseOpen(false)}
                accessToken={accessToken ?? null}
                onCourseCreated={async () => {
                    setCreateCourseOpen(false);
                    await loadCourses();
                    setCoursesOpen(true);
                }}
            />
        </>
    );
}

export default SideTabNav;
