import {useState} from "react";
import {
    createCourseSessionFromTimeEdit,
} from "../../api/taConstraintsApi.ts";
import {X} from "lucide-react";

interface ImportTimeEditCoursePopUpProps {
    isOpen: boolean;
    onClose: () => void;
    accessToken: string | null;
    currentCourseId: string;
    onImportedTimeEditCourse?: (courseCode: string) => void | Promise<void>;
}

export function ImportTimeEditCoursePopUp({ isOpen, onClose, currentCourseId, onImportedTimeEditCourse, accessToken }: ImportTimeEditCoursePopUpProps) {

    const [courseCode, setCourseCode] = useState<string>("");

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    if (!isOpen) return null;

    const handleSubmit = async () => {
        if(!courseCode.trim()){
            setErrorMessage("Please enter a valid code");
            return;
        }

        setIsSubmitting(true);
        setErrorMessage(null);

        try {
            await createCourseSessionFromTimeEdit(
                currentCourseId,
                {courseCode: courseCode.trim()},
                accessToken,
            )
            onImportedTimeEditCourse?.(courseCode.trim());
            onClose();
        } catch (error) {
            setErrorMessage("Could not import from TimeEdit")
            console.error(error);
        } finally {
            setIsSubmitting(false);
        }
    }

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
                    className="absolute right-4 top-4 inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-slate-600 cursor-pointer"
                    aria-label="Close popup"
                >
                    <X className="h-4 w-4" />
                </button>
                <div className="px-6 pt-6 pb-4">
                    <h2 className="text-xl font-semibold text-slate-900">Import TimeEdit schedule</h2>
                    <p className="mt-1 text-sm text-slate-500">
                        Write courseCode to import your classes and exercise sessions as times you can't work hard constraints. You can remove them later.
                    </p>
                </div>

                <div className="px-6 pb-6 space-y-4">
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Course code
                        </label>
                        <input
                            type="text"
                            value={courseCode}
                            onChange={(e) =>
                                setCourseCode(e.target.value)
                            }
                            className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-[#003b5c]"
                            placeholder="DAT216"
                        />

                        <button
                            type="button"
                            onClick={handleSubmit}
                            disabled={isSubmitting}
                            className="mt-4 rounded-2xl bg-[#003b5c] px-5 py-3 text-sm font-semibold text-white hover:bg-[#002741] disabled:opacity-50 cursor-pointer"
                        >
                            {isSubmitting ? "Importing..." : "Import"}
                        </button>

                    </div>
            </div>
                {errorMessage && (
                    <p className="text-sm text-red-600">{errorMessage}</p>
                )}
        </div>
        </div>

    )

}