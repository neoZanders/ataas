import { useCallback, useEffect, useState, type FormEvent } from "react";
import { X } from "lucide-react";
import { MarkdownText } from "./MarkdownText";

interface AddAnnouncementPopUpProps {
    isOpen: boolean;
    onClose: () => void;
    onCreate: (data: { title: string; body: string; sendByEmail: boolean }) => void;
}

export function AddAnnouncementPopUp({ isOpen, onClose, onCreate }: AddAnnouncementPopUpProps) {
    const [title, setTitle] = useState("");
    const [body, setBody] = useState("");
    const [sendByEmail, setSendByEmail] = useState(false);
    const [error, setError] = useState("");

    function resetState() {
        setTitle("");
        setBody("");
        setSendByEmail(false);
        setError("");
    };

    const handleClose = useCallback(() => {
        onClose();
    }, [onClose]);

    const handleCancel = useCallback(() => {
        resetState();
        onClose();
    }, [onClose]);

    useEffect(() => {
        if (!isOpen) return;

        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") handleClose();
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isOpen, handleClose]);

    if (!isOpen) return null;

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!title.trim() || !body.trim()) {
            setError("Title and body are required.");
            return;
        }

        onCreate({ title: title.trim(), body: body.trim(), sendByEmail });
        resetState();
        handleClose();
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/20 px-4 backdrop-blur-sm"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget) handleClose();
            }}
        >
            <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-xl ring-1 ring-slate-200">
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">Create Announcement</h2>
                    <button
                        type="button"
                        className="inline-flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 hover:cursor-pointer"
                        onClick={handleClose}
                        aria-label="Close popup"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <label className="block text-sm font-semibold text-slate-700">
                        Title
                        <input
                            value={title}
                            onChange={(event) => setTitle(event.target.value)}
                            className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                            placeholder="Announcement title"
                        />
                    </label>

                    <div className="grid gap-4 md:grid-cols-2">
                        <label className="block text-sm font-semibold text-slate-700">
                            Body (Markdown supported)
                            <textarea
                                value={body}
                                onChange={(event) => setBody(event.target.value)}
                                rows={8}
                                className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                                placeholder="Write your announcement in markdown..."
                            />
                        </label>
                        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                                Preview
                            </p>
                            <div className="mt-2 text-sm text-slate-800">
                                {body.trim() ? (
                                    <MarkdownText content={body} />
                                ) : (
                                    <p className="text-slate-500">Nothing to preview yet.</p>
                                )}
                            </div>
                        </div>
                    </div>

                    <label className="flex items-center gap-2 text-sm text-slate-700">
                        <input
                            type="checkbox"
                            checked={sendByEmail}
                            onChange={(event) => setSendByEmail(event.target.checked)}
                            className="h-4 w-4 rounded border-slate-300 accent-[#003b5c] hover:cursor-pointer"
                        />
                        Send by email as well
                    </label>

                    {error && <p className="text-sm text-rose-600">{error}</p>}

                    <div className="flex items-center justify-end gap-3">
                        <button
                            type="button"
                            className="rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 hover:cursor-pointer"
                            onClick={handleCancel}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="rounded-full bg-[#003b5c] px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] hover:cursor-pointer"
                        >
                            Create
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
