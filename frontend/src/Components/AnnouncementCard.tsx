import { useMemo, useState } from "react";
import type { Announcement } from "./CourseResponsibleAnnouncementPage";
import { MarkdownText } from "./MarkdownText";

type AnnouncementCardProps = {
    announcement: Announcement;
};

function formatDate(value: string) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
}

function getPreview(markdown: string, maxLength: number) {
    const trimmed = markdown.trim();
    if (trimmed.length <= maxLength) return trimmed;
    const slice = trimmed.slice(0, maxLength);

    //Safe Slicing to avoid slicing in the middle of markdown notation
    const lastSpace = slice.lastIndexOf(" ");
    const safeSlice = lastSpace > 60 ? slice.slice(0, lastSpace) : slice;
    return `${safeSlice}...`;
}

export function AnnouncementCard({ announcement }: AnnouncementCardProps) {
    const [expanded, setExpanded] = useState(false);
    const preview = useMemo(() => getPreview(announcement.body, 160), [announcement.body]);
    const showExpandButton = announcement.body.length >= 160;
    const markdownContent = expanded ? announcement.body : preview;

    return (
        <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                    <h3 className="text-lg font-semibold text-slate-900">{announcement.title}</h3>
                    <p className="text-xs text-slate-500">{formatDate(announcement.createdAt)}</p>
                </div>
                {showExpandButton && (
                    <button
                        type="button"
                        className="rounded-full border border-slate-300 px-3 py-1 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 hover:cursor-pointer"
                        onClick={() => setExpanded((prev) => !prev)}
                    >
                        {expanded ? "Minimize" : "Expand"}
                    </button>
                )}
            </div>

            <div className="mt-3 text-sm text-slate-700">
                {markdownContent ? (
                    <MarkdownText content={markdownContent} />
                ) : (
                    <p>No content.</p>
                )}
            </div>

            {announcement.sendByEmail && (
                <p className="mt-3 text-xs font-semibold text-emerald-700">Sent by email</p>
            )}
        </article>
    );
}
