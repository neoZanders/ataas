type CoursesSidePanelOption =
    | {
    type: "course";
    label: string;
    courseId: string;
    assignmentStatus: "OWNER" | "JOINED" | "INVITED" | string;
}
    | {
    type: "action";
    label: string;
    action: "create-course" | "join-course";
};

interface CoursesSidePanelProps {
    title?: string;
    options: CoursesSidePanelOption[];
    selectedCourseId: string | null;
    onSelectCourse: (courseId: string) => void;
    onOpenCreateCourse?: () => void;
    onOpenJoinCourse?: () => void;
    onJoinCourse?: (courseId: string) => void | Promise<void>;
    joiningCourseId?: string | null;
}

export function CoursesSidePanel({
                                     options,
                                     title = "Courses",
                                     selectedCourseId,
                                     onSelectCourse,
                                     onOpenCreateCourse,
                                     onOpenJoinCourse,
                                     onJoinCourse,
                                     joiningCourseId,
                                 }: CoursesSidePanelProps) {
    return (
        <div className="h-full w-full bg-white">
            <div className="px-4 pt-4 pb-4 text-center border-8 border-white rounded-4xl bg-[#003b5c]">
                <h2 className="text-xl font-semibold text-white">{title}</h2>
            </div>

            <div className="flex flex-col gap-1 px-2 py-2">
                {options.map((option, index) => {
                    if (option.type === "action") {
                        return (
                            <button
                                key={`${option.action}-${index}`}
                                type="button"
                                onClick={() => {
                                    if (option.action === "create-course") {
                                        onOpenCreateCourse?.();
                                    } else if (option.action === "join-course") {
                                        onOpenJoinCourse?.();
                                    }
                                }}
                                className="w-full rounded-xl px-4 py-3 text-center bg-slate-300 text-lg transition text-slate-700 hover:bg-slate-100"
                            >
                                {option.label}
                            </button>
                        );
                    }

                    const selected = option.courseId === selectedCourseId;
                    const canSelect =
                        option.assignmentStatus === "OWNER" ||
                        option.assignmentStatus === "JOINED";
                    const isInvited = option.assignmentStatus === "INVITED";
                    const isJoining = joiningCourseId === option.courseId;

                    return (
                        <div
                            key={option.courseId}
                            className={[
                                "w-full rounded-xl px-4 py-3 transition",
                                selected
                                    ? "bg-[#003b5c] text-white"
                                    : "bg-slate-300 text-slate-700",
                            ].join(" ")}
                        >
                            <div className="flex items-center justify-between gap-3">
                                <button
                                    type="button"
                                    onClick={() => {
                                        if (!canSelect) return;
                                        onSelectCourse(option.courseId);
                                    }}
                                    disabled={!canSelect}
                                    className={[
                                        "min-w-0 flex-1 text-left text-lg",
                                        canSelect
                                            ? "cursor-pointer"
                                            : "cursor-not-allowed opacity-70",
                                    ].join(" ")}
                                >
                                    <div className="truncate">{option.label}</div>

                                    {isInvited && (
                                        <div className="mt-1 text-xs font-medium opacity-80">
                                            Invited
                                        </div>
                                    )}
                                </button>

                                {isInvited && (
                                    <button
                                        type="button"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onJoinCourse?.(option.courseId);
                                        }}
                                        disabled={isJoining}
                                        className="shrink-0 rounded-lg bg-[#003b5c] px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-[#002741] disabled:opacity-50"
                                    >
                                        {isJoining ? "Joining..." : "Join"}
                                    </button>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}