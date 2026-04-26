import {
    Search,
    UserCircle2,
    Trash2,
} from "lucide-react";
import SideTabNav from "./SideTabNav.tsx";
import { useState } from "react";

type TAConstraint = {
    id: string;
    name: string;
    constraints: string[];
};

type SessionConstraint = {
    id: string;
    minTAs: string;
    maxTAs: string;
    date: string;
    session: string;
}

const TAConstraints: TAConstraint[] = [
    {
        id: "1",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
    {
        id: "2",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
    {
        id: "3",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
    {
        id: "4",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
    {
        id: "5",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
    {
        id: "6",
        name: "Jane Doe",
        constraints: ["Cannot work Mondays", "Max 2 sessions/week"],
    },
];

const initialSessionConstraints: SessionConstraint[] = [
    {
        id: "1",
        minTAs: "1",
        maxTAs: "3",
        date: "2026-03-20",
        session: "Lab 1",
    },
    {
        id: "2",
        minTAs: "2",
        maxTAs: "5",
        date: "2026-03-21",
        session: "Lab 2",
    },
];


export function CourseResponsibleConstraintsPage() {
    const [sessionConstraints, setSessionConstraints] = useState<SessionConstraint[]>(initialSessionConstraints);

    const handleDeleteSession = (id: string) => {
        setSessionConstraints((prev) =>
            prev.filter((s) => s.id !== id)
        );
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">Constraints</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Course responsible’s overview of TA and session constraints
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"></div>

                        <div className="grid grid-cols-2 rounded-2xl border border-slate-200 overflow-hidden">
                            <div className="bg-slate align-top border-r border-slate-200">
                                <div className="min-w-[340px] bg-slate-50 border-b border-slate-200 px-5 py-4 h-28 space-y-3">
                                    <p className="flex w-full items-center gap-2 text-left text-sm font-semibold text-slate-700 transition hover:text-[#003b5c]">
                                        TA Constraints
                                    </p>

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

                                {TAConstraints.map((row) => (
                                    <div key={row.id} className="rounded-3xl border border-r border-slate-200 bg-white p-4 m-2">
                                        <div className="flex items-center gap-3 text-sm font-semibold text-slate-900">
                                            <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                                <UserCircle2 className="h-5 w-5" />
                                            </span>

                                            {row.name}
                                        </div>

                                        <ul className="mt-1 pl-5 space-y-0.5 list-disc text-sm text-slate-600">
                                            {row.constraints.map((c, i) => <li key={i}>{c}</li>)}
                                        </ul>
                                    </div>
                                ))}
                                </div>

                                <div className="bg-slate align-top">
                                    <div className="min-w-[340px] bg-slate-50 border-b border-slate-200 px-5 py-4 h-28">
                                        <p className="flex w-full items-center gap-2 text-left text-sm font-semibold text-slate-700 transition hover:text-[#003b5c]">
                                            Session Constraints
                                        </p>
                                    </div>

                                {sessionConstraints.map((row) => (
                                    <div key={row.id} className="relative rounded-3xl border border-slate-200 p-4 bg-white m-2">
                                        <button
                                            onClick={() => handleDeleteSession(row.id)}
                                            className="absolute inset-y-0 right-2 my-auto text-slate-400 hover:text-red-500 transition"
                                        >
                                            <Trash2 className="h-4 w-4 text-slate-600" />
                                        </button>
                                        <p className="flex items-center pl-1 gap-3 text-sm font-semibold text-slate-900">{row.session}</p>
                                        <p><span className="mt-1 pl-1 space-y-0.5 list-disc text-sm text-slate-600">{row.date}</span></p>
                                        <p><span className="mt-1 pl-1 space-y-0.5 list-disc text-sm text-slate-600">Min TAs: {row.minTAs}</span></p>
                                        <p><span className="mt-1 pl-1 space-y-0.5 list-disc text-sm text-slate-600">Max TAs: {row.maxTAs}</span> </p>
                                    </div>
                                ))}

                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    );
}