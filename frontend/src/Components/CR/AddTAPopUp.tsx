import { useCallback, useEffect, useState } from "react";
import { Plus, X } from "lucide-react";

interface AddTAPopUpProps {
    isOpen: boolean;
    onClose: () => void;
}

export function AddTAPopUp({ isOpen, onClose }: AddTAPopUpProps) {
    const [cids, setCids] = useState<string[]>([""]);

    const handleClose = useCallback(() => {
        setCids([""]);
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

    if (!isOpen) return null;

    const handleCidChange = (index: number, value: string) => {
        setCids((prev) => prev.map((cid, i) => (i === index ? value : cid)));
    };

    const handleAddStudent = () => {
        setCids((prev) => [...prev, ""]);
    };

    const handleRemoveStudent = (indexToRemove: number) => {
        setCids((prev) => prev.filter((_, index) => index !== indexToRemove));
    };

    const handleInvite = () => {
        // send to backend here
        handleClose();
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/20 px-4 backdrop-blur-sm"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) handleClose();
            }}
        >
            <div className="relative w-full max-w-md rounded-3xl bg-white p-6 shadow-xl ring-1 ring-slate-200">
                <button
                    type="button"
                    onClick={handleClose}
                    className="absolute right-4 top-4 inline-flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
                    aria-label="Close popup"
                >
                    <X className="h-4 w-4" />
                </button>

                <div className="mb-5 pr-10">
                    <h2 className="text-xl font-semibold text-slate-900">Add TA</h2>
                    <p className="mt-1 text-sm text-slate-500">
                        Enter one or more student CIDs.
                    </p>
                </div>

                <div className="space-y-3">
                    {cids.map((cid, index) => (
                        <div key={index}>
                            <label
                                htmlFor={`cid-${index}`}
                                className="mb-1.5 block text-sm font-medium text-slate-700"
                            >
                                Enter CID
                            </label>

                            <div className="flex items-center gap-2">
                                <input
                                    id={`cid-${index}`}
                                    type="text"
                                    value={cid}
                                    onChange={(e) => handleCidChange(index, e.target.value)}
                                    placeholder="e.g. abcd1234"
                                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                />

                                {index > 0 && (
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveStudent(index)}
                                        className="inline-flex h-[46px] shrink-0 items-center justify-center rounded-2xl border border-slate-200 px-3 text-sm font-semibold text-slate-600 transition hover:border-red-300 hover:bg-red-50 hover:text-red-600"
                                        aria-label={`Remove student ${index + 1}`}
                                    >
                                        <X className="h-4 w-4" />
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>

                <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <button
                        type="button"
                        onClick={handleAddStudent}
                        className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                    >
                        <Plus className="h-4 w-4" />
                        Add student
                    </button>

                    <button
                        type="button"
                        onClick={handleInvite}
                        className="inline-flex items-center justify-center rounded-2xl bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                    >
                        Invite
                    </button>
                </div>
            </div>
        </div>
    );
}