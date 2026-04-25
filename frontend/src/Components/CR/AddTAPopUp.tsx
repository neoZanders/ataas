import { useCallback, useEffect, useState } from "react";
import { Plus, X } from "lucide-react";
import {inviteCrs, inviteTas} from "../../api/courseAssignmentApi.ts";

interface AddTAPopUpProps {
    isOpen: boolean;
    onClose: () => void;
    courseId: string | null;
    accessToken: string | null;
}

export function AddTAPopUp({
                                isOpen,
                                onClose,
                                courseId,
                                accessToken,

                           }: AddTAPopUpProps) {
    const [taEmail, setTaEmail] = useState<string[]>([""]);
    const [crEmail, setCrEmail] = useState<string[]>([""]);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleClose = useCallback(() => {
        if (isSubmitting) return;

        setTaEmail([""]);
        setCrEmail([""]);
        setError(null)
        onClose();
    }, [onClose, isSubmitting]);

    useEffect(() => {
        if (!isOpen) return;

        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") handleClose();
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isOpen, handleClose]);

    if (!isOpen) return null;

    const getCleanEmails = (emails: string[]) => {
        return emails
            .map((email) => email.trim())
            .filter((email) => email.length > 0);
    }

    const handleEmailChangeTa = (index: number, value: string) => {
        setTaEmail((prev) => prev.map((cid, i) => (i === index ? value : cid)));
    };

    const handleAddTa = () => {
        setTaEmail((prev) => [...prev, ""]);
    };

    const handleRemoveTa = (indexToRemove: number) => {
        setTaEmail((prev) => prev.filter((_, index) => index !== indexToRemove));
    };

    const handleEmailChangeCr = (index: number, value: string) => {
        setCrEmail((prev) => prev.map((cid, i) => (i === index ? value : cid)));
    };

    const handleAddCr = () => {
        setCrEmail((prev) => [...prev, ""]);
    };

    const handleRemoveCr = (indexToRemove: number) => {
        setCrEmail((prev) => prev.filter((_, index) => index !== indexToRemove));
    };

    const handleInvite = async () => {
        if(!courseId) {
            setError("No course selected");
            return;
        }

        const cleanTaEmails = getCleanEmails(taEmail)
        const cleanCrEmails = getCleanEmails(crEmail)

        if(cleanCrEmails.length === 0 && cleanTaEmails.length === 0) {
            setError("No emails selected");
            return;
        }

        try {
            setIsSubmitting(true);
            setError(null);

            await Promise.all([
                ...cleanTaEmails.map((email) =>
                inviteTas(courseId,accessToken, {taEmail : email }),
                ),
                ...cleanCrEmails.map((email) =>
                inviteCrs(courseId,accessToken, {crEmail : email}))
            ]);

            handleClose();
        }
        catch(error) {
            console.error(error);
            setError("could not send one or more invitations");
        }
        finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/20 px-4 backdrop-blur-sm"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) handleClose();
            }}
        >
            <div className="relative w-full max-w-xl rounded-3xl bg-white p-6 shadow-xl ring-1 ring-slate-200">
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
                        Enter one or more student email.
                    </p>
                </div>

                <div className="mb-5 space-y-3">
                    {taEmail.map((email, index) => (
                        <div key={index}>
                            <label
                                htmlFor={`cid-${index}`}
                                className="mb-1.5 block text-sm font-medium text-slate-700"
                            >
                                Enter TA email:
                            </label>

                            <div className="flex items-center gap-2">
                                <input
                                    id={`ta-email-${index}`}
                                    type="email"
                                    value={email}
                                    onChange={(e) => handleEmailChangeTa(index, e.target.value)}
                                    placeholder="e.g. benim@student.test.se"
                                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                />

                                {index > 0 && (
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveTa(index)}
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

                <p className="mt-1 mb-5 text-sm text-slate-500">
                    Enter one or more course responsible email.
                </p>

                <div className="space-y-3">
                    {crEmail.map((email, index) => (
                        <div key={index}>
                            <label
                                htmlFor={`cid-${index}`}
                                className="mb-1.5 block text-sm font-medium text-slate-700"
                            >
                                Enter course responsible email:
                            </label>

                            <div className="flex items-center gap-2">
                                <input
                                    id={`cr-email-${index}`}
                                    type="email"
                                    value={email}
                                    onChange={(e) => handleEmailChangeCr(index, e.target.value)}
                                    placeholder="e.g. benim@student.test.se"
                                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                />

                                {index > 0 && (
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveCr(index)}
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
                        onClick={handleAddTa}
                        className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                    >
                        <Plus className="h-4 w-4" />
                        Add student
                    </button>

                    <button
                        type="button"
                        className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-[#003b5c] hover:text-[#003b5c]"
                        onClick={handleAddCr}
                    >
                        <Plus className="h-4 w-4" />
                        Add Course Responsible
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