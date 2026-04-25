import {
    Search,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
    UserCircle2,
} from "lucide-react";
import SideTabNav from "../SideTabNav.tsx";
import {useEffect, useState} from "react";
import {useAuth} from "../AuthContext.tsx";
import {useCurrentCourse} from "../CurrentCourseContext.tsx";
import {type CourseResponse, getCourseById} from "../../api/coursesApi.ts";
import {
    getListCourseMembers,
    type TaCourseAssignment
} from "../../api/courseAssignmentApi.ts";

type TARecord = {
    id: string;
    name: string;
    budgetHours: number;

};

type SortKey = "name" | "budgetHours" ;
type SortDirection = "asc" | "desc";

const headerButtonClass =
    "flex w-full items-center gap-2 text-left text-sm font-semibold text-slate-700 transition hover:text-[#003b5c]";


export function TATaListPage() {
    const [sortKey ] = useState<SortKey>("name");
    const [sortDirection] = useState<SortDirection>("asc");

    const { accessToken } = useAuth();
    const {currentCourseId} = useCurrentCourse();
    const [course, setCourse] = useState<CourseResponse | null>(null);

    const [taList, setTaList] = useState<TARecord[]>([]);

    function mapTaCourseAssignmentToUser(taMember: TaCourseAssignment): TARecord {
        return {
            id: taMember.ta.id,
            name: taMember.ta.name,
            budgetHours: taMember.maxHours
        }
    }


    const renderSortIcon = (key: SortKey) => {
        if (sortKey !== key) {
            return <ArrowUpDown className="h-4 w-4" />;
        }

        return sortDirection === "asc" ? (
            <ArrowUp className="h-4 w-4" />
        ) : (
            <ArrowDown className="h-4 w-4" />
        );
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
        const loadTAList = async () => {
            if (!currentCourseId || !accessToken) return;

            try {
                const response = await getListCourseMembers(
                    currentCourseId,
                    accessToken,
                )

                const listTaCourseAssignmentSlots = response.taCourseAssignments

                setTaList(
                    listTaCourseAssignmentSlots.map(mapTaCourseAssignmentToUser)
                );

            } catch (error){
                console.error("Failed to load list", error);
            }

        };
        loadTAList();
    }, [currentCourseId, accessToken]);

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">TA List</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            TA list overview
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="overflow-x-auto rounded-2xl border border-slate-200">
                            <table className="w-full min-w-[1100px] border-separate border-spacing-0">
                                <thead>
                                <tr className="bg-slate-50 align-top">
                                    <th className="min-w-[340px] border-b border-r border-slate-200 px-5 py-4">
                                        <div className="space-y-3">
                                            <button
                                                type="button"
                                                className={headerButtonClass}
                                            >
                                                TA List
                                                {renderSortIcon("name")}
                                            </button>

                                            <div className="relative">
                          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                            <Search className="h-4 w-4" />
                          </span>
                                                <input
                                                    type="text"
                                                    placeholder="Search for TA..."
                                                    className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-3 text-sm font-normal text-slate-700 placeholder:text-slate-400 outline-none"
                                                />
                                            </div>
                                        </div>
                                    </th>

                                    <th className="min-w-[180px] border-b border-r border-slate-200 px-5 py-4">
                                        <button
                                            type="button"
                                            className={headerButtonClass}
                                        >
                                            Budget hours
                                            {renderSortIcon("budgetHours")}
                                        </button>
                                    </th>

                                </tr>
                                </thead>

                                <tbody>
                                {taList.map((row, index) => (
                                    <tr
                                        key={row.id}
                                        className={index % 2 === 0 ? "bg-white" : "bg-slate-50/40"}
                                    >
                                        <td className="border-b border-r border-slate-100 px-5 py-4">
                                            <div className="flex items-center gap-3 text-sm font-medium text-slate-900">
                          <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                            <UserCircle2 className="h-5 w-5" />
                          </span>
                                                {row.name}
                                            </div>
                                        </td>

                                        <td className="border-b border-r border-slate-100 px-5 py-4 text-sm text-slate-700">
                                            {row.budgetHours}
                                        </td>

                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </section>
                </div>
            </main>

        </div>
    );
}