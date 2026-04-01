
type CoursesSidePanelOptions = {
    courseCode: string;
    value: string;  // TODO later change to the courseType value in Swagger
}

interface CoursesSidePanelProps {
    title?:string;
    options: CoursesSidePanelOptions[];
    value: string | null;
    onChange: (value: string) => void;
}

export function CoursesSidePanel({
    options,
    title = "Courses",
    value,
    onChange,
    }: CoursesSidePanelProps) {


    return (
        <div className="h-full w-full bg-white">
            <div className="px-4 pt-4 pb-4 text-center border-8 border-white rounded-4xl bg-[#003b5c]">
                <h2 className="text-xl font-semibold text-white">
                    {title}
                </h2>
            </div>

            <div className="flex flex-col gap-1 px-2 py-2">
                {options.map((option) => {
                    const selected = value === option.value;

                    return (
                        <button
                            key={option.value}
                            type="button"
                            onClick={() => onChange(option.value)}
                            className={[
                                "w-full rounded-xl px-4 py-3 text-center bg-slate-300 text-lg transition",
                                selected
                                    ? "bg-[#003b5c] text-white"
                                    : "text-slate-700 hover:bg-slate-100",
                            ].join(" ")}
                        >
                            {option.courseCode}
                        </button>
                    );
                })}
            </div>
        </div>
    );

}