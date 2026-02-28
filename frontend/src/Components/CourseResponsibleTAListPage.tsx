import {
    Search,
    Plus,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
    UserCircle2,
} from "lucide-react";
import SideTabNav from "./SideTabNav.tsx";
import { useMemo, useState } from "react";
import { AddTAPopUp } from "./AddTAPopUp.tsx";

type TARecord = {
    id: string;
    name: string;
    budgetHours: number;
    allocatedHours: number;
    administrationHours: number;
};

type SortKey = "name" | "budgetHours" | "allocatedHours" | "administrationHours";
type SortDirection = "asc" | "desc";

// mock data will remove later
const rows: TARecord[] = [
    {
        id: "1",
        name: "Jane Doe",
        budgetHours: 80,
        allocatedHours: 52,
        administrationHours: 8,
    },
    {
        id: "2",
        name: "Jane Doe",
        budgetHours: 60,
        allocatedHours: 41,
        administrationHours: 6,
    },
    {
        id: "3",
        name: "Jane Doe",
        budgetHours: 100,
        allocatedHours: 73,
        administrationHours: 12,
    },
    {
        id: "4",
        name: "Jane Doe",
        budgetHours: 75,
        allocatedHours: 30,
        administrationHours: 5,
    },
    {
        id: "5",
        name: "Jane Doe",
        budgetHours: 90,
        allocatedHours: 68,
        administrationHours: 10,
    },
    {
        id: "6",
        name: "Jane Doe",
        budgetHours: 85,
        allocatedHours: 49,
        administrationHours: 9,
    },
];

const headerButtonClass =
    "flex w-full items-center gap-2 text-left text-sm font-semibold text-slate-700 transition hover:text-[#003b5c]";

export function CourseResponsibleTAListPage() {
    const [isPopUpOpen, setIsPopUpOpen] = useState(false);
    const [sortKey, setSortKey] = useState<SortKey>("name");
    const [sortDirection, setSortDirection] = useState<SortDirection>("asc");

    const handleSort = (key: SortKey) => {
        if (sortKey === key) {
            setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
            return;
        }

        setSortKey(key);
        setSortDirection("asc");
    };

    const sortedRows = useMemo(() => {
        const sorted = [...rows];

        sorted.sort((a, b) => {
            const aValue = a[sortKey];
            const bValue = b[sortKey];

            let result = 0;

            if (typeof aValue === "number" && typeof bValue === "number") {
                result = aValue - bValue;
            } else {
                result = String(aValue).localeCompare(String(bValue));
            }

            return sortDirection === "asc" ? result : -result;
        });

        return sorted;
    }, [sortKey, sortDirection]);

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

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">TA List</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Course responsible TA list overview
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                            <button
                                type="button"
                                onClick={() => {
                                    setIsPopUpOpen(true);
                                }}
                                className="inline-flex items-center justify-center gap-2 rounded-full bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                            >
                                <Plus className="h-4 w-4" />
                                Add TA
                            </button>

                            <button
                                type="button"
                                className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-[#003b5c] hover:text-[#003b5c]"
                            >
                                <Plus className="h-4 w-4" />
                                Create custom column
                            </button>
                        </div>

                        <div className="overflow-x-auto rounded-2xl border border-slate-200">
                            <table className="w-full min-w-[1100px] border-separate border-spacing-0">
                                <thead>
                                <tr className="bg-slate-50 align-top">
                                    <th className="min-w-[340px] border-b border-r border-slate-200 px-5 py-4">
                                        <div className="space-y-3">
                                            <button
                                                type="button"
                                                onClick={() => handleSort("name")}
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
                                            onClick={() => handleSort("budgetHours")}
                                            className={headerButtonClass}
                                        >
                                            Budget hours
                                            {renderSortIcon("budgetHours")}
                                        </button>
                                    </th>

                                    <th className="min-w-[190px] border-b border-r border-slate-200 px-5 py-4">
                                        <button
                                            type="button"
                                            onClick={() => handleSort("allocatedHours")}
                                            className={headerButtonClass}
                                        >
                                            Allocated hours
                                            {renderSortIcon("allocatedHours")}
                                        </button>
                                    </th>

                                    <th className="min-w-[220px] border-b border-slate-200 px-5 py-4">
                                        <button
                                            type="button"
                                            onClick={() => handleSort("administrationHours")}
                                            className={headerButtonClass}
                                        >
                                            Administration hours
                                            {renderSortIcon("administrationHours")}
                                        </button>
                                    </th>
                                </tr>
                                </thead>

                                <tbody>
                                {sortedRows.map((row, index) => (
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

                                        <td className="border-b border-r border-slate-100 px-5 py-4 text-sm text-slate-700">
                                            {row.allocatedHours}
                                        </td>

                                        <td className="border-b border-slate-100 px-5 py-4 text-sm text-slate-700">
                                            {row.administrationHours}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </section>
                </div>
            </main>

            <AddTAPopUp isOpen={isPopUpOpen} onClose={() => setIsPopUpOpen(false)} />
        </div>
    );
}