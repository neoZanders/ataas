import { useState } from "react";
import { X } from "lucide-react";
import {createCourse, type CourseRequest} from "../api/coursesApi.ts";

interface CreateCoursePopUpProps {
    isOpen: boolean;
    onClose: () => void;
    accessToken: string | null;
    onCourseCreated?: (courseId: string) => void;
}

export function CreateCoursePopUp({
                                      isOpen,
                                      onClose,
                                      accessToken,
                                      onCourseCreated,
                                  }: CreateCoursePopUpProps) {
    const [form, setForm] = useState<CourseRequest>({
        courseCode: "",
        description: "",
        canTASeeAllSchedules: false,
        canTACreateAnnouncements: false,
        startDate: "",
        endDate: "",
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    if (!isOpen) return null;

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setErrorMessage(null);

        try {
            const created = await createCourse(form, accessToken);
            onCourseCreated?.(created.courseId);
            onClose();
            setForm({
                courseCode: "",
                description: "",
                canTASeeAllSchedules: false,
                canTACreateAnnouncements: false,
                startDate: "",
                endDate: "",
            });
        } catch (error) {
            setErrorMessage("Could not create course.");
            console.error(error);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/30 backdrop-blur-sm px-4"
            onMouseDown={(e) => {
                if (e.target === e.currentTarget) onClose();
            }}
        >
            <div className="relative w-full max-w-2xl rounded-3xl bg-white shadow-xl ring-1 ring-slate-200">
                <button
                    type="button"
                    onClick={onClose}
                    className="absolute right-4 top-4 inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                    aria-label="Close popup"
                >
                    <X className="h-4 w-4" />
                </button>

                <div className="px-6 pt-6 pb-4">
                    <h2 className="text-xl font-semibold text-slate-900">Create course</h2>
                    <p className="mt-1 text-sm text-slate-500">
                        Fill in the details for the new course.
                    </p>
                </div>

                <div className="px-6 pb-6 space-y-4">
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Course code
                        </label>
                        <input
                            type="text"
                            value={form.courseCode}
                            onChange={(e) =>
                                setForm((prev) => ({ ...prev, courseCode: e.target.value }))
                            }
                            className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-[#003b5c]"
                            placeholder="DAT216"
                        />
                    </div>

                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Description
                        </label>
                        <textarea
                            value={form.description}
                            onChange={(e) =>
                                setForm((prev) => ({ ...prev, description: e.target.value }))
                            }
                            className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-[#003b5c]"
                            rows={4}
                        />
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                Start date
                            </label>
                            <input
                                type="date"
                                value={form.startDate}
                                onChange={(e) =>
                                    setForm((prev) => ({ ...prev, startDate: e.target.value }))
                                }
                                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-[#003b5c]"
                            />
                        </div>

                        <div>
                            <label className="mb-1.5 block text-sm font-medium text-slate-700">
                                End date
                            </label>
                            <input
                                type="date"
                                value={form.endDate}
                                onChange={(e) =>
                                    setForm((prev) => ({ ...prev, endDate: e.target.value }))
                                }
                                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-[#003b5c]"
                            />
                        </div>
                    </div>

                    <label className="flex items-center gap-2 text-sm text-slate-700">
                        <input
                            type="checkbox"
                            checked={form.canTASeeAllSchedules}
                            onChange={(e) =>
                                setForm((prev) => ({
                                    ...prev,
                                    canTASeeAllSchedules: e.target.checked,
                                }))
                            }
                            className="h-4 w-4 rounded border-slate-300 accent-[#003b5c]"
                        />
                        TAs can see all schedules
                    </label>

                    <label className="flex items-center gap-2 text-sm text-slate-700">
                        <input
                            type="checkbox"
                            checked={form.canTACreateAnnouncements}
                            onChange={(e) =>
                                setForm((prev) => ({
                                    ...prev,
                                    canTACreateAnnouncements: e.target.checked,
                                }))
                            }
                            className="h-4 w-4 rounded border-slate-300 accent-[#003b5c]"
                        />
                        TAs can create announcements
                    </label>

                    {errorMessage && (
                        <p className="text-sm text-red-600">{errorMessage}</p>
                    )}

                    <div className="flex justify-end gap-3 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 hover:border-[#003b5c] hover:text-[#003b5c]"
                        >
                            Cancel
                        </button>

                        <button
                            type="button"
                            onClick={handleSubmit}
                            disabled={isSubmitting}
                            className="rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white hover:bg-[#002741] disabled:opacity-50"
                        >
                            {isSubmitting ? "Creating..." : "Create course"}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}