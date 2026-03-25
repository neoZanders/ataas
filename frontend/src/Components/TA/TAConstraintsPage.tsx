import SideTabNav from "../SideTabNav.tsx";
import { Plus } from "lucide-react";
import { useState } from "react";
import { AddTAConstraintsPopUp } from "./AddTAConstraintsPopUp.tsx";

export function TAConstraintsPage() {
    const [isPopUpOpen, setIsPopUpOpen] = useState(false);

    const [minHoursWork, setMinHoursWork] = useState<number>(0);
    const [maxHoursWork, setMaxHoursWork] = useState<number>(0);

    const handleSaveMinHours = () => {
        console.log("Saved minimum hours:", minHoursWork);
    };

    const handleSaveMaxHours = () => {
        console.log("Saved maximum hours:", maxHoursWork);
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen py-4 pl-[104px]">
                <div className="mb-6 text-center">
                    <h1 className="text-3xl font-bold text-slate-900">TA Constraints</h1>
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
                            Add Constraints
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

                        <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <label className="mb-2 block text-sm font-medium text-slate-700">
                                    Minimum hours
                                </label>

                                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                    <div className="flex items-center gap-3">
                                        <input
                                            type="number"
                                            min={0}
                                            value={minHoursWork}
                                            onChange={(e) => setMinHoursWork(Number(e.target.value))}
                                            className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        />
                                        <span className="text-sm text-slate-600">hours</span>
                                    </div>

                                    <button
                                        type="button"
                                        onClick={handleSaveMinHours}
                                        className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                                    >
                                        Save
                                    </button>
                                </div>
                            </div>

                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <label className="mb-2 block text-sm font-medium text-slate-700">
                                    Maximum hours
                                </label>

                                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                    <div className="flex items-center gap-3">
                                        <input
                                            type="number"
                                            min={0}
                                            value={maxHoursWork}
                                            onChange={(e) => setMaxHoursWork(Number(e.target.value))}
                                            className="w-32 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none focus:border-[#003b5c]"
                                        />
                                        <span className="text-sm text-slate-600">hours</span>
                                    </div>

                                    <button
                                        type="button"
                                        onClick={handleSaveMaxHours}
                                        className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                                    >
                                        Save
                                    </button>
                                </div>
                            </div>
                        </div>
                    </section>
                </div>
            </main>

            <AddTAConstraintsPopUp
                isOpen={isPopUpOpen}
                onClose={() => setIsPopUpOpen(false)}
            />
        </div>
    );
}