import { Plus } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import SideTabNav from "../SideTabNav.tsx";
import { AddAnnouncementPopUp } from "../AddAnnouncementPopUp.tsx";
import { AnnouncementCard } from "../AnnouncementCard.tsx";
import { useAuth } from "../AuthContext.tsx";
import { useCurrentCourse } from "../CurrentCourseContext.tsx";
import { createAnnouncement, getAnnouncements } from "../../api/announcementsApi.ts";
import { getCourseById, type CourseResponse } from "../../api/coursesApi.ts";
import type { Announcement, CreateAnnouncementRequest } from "../../types/announcement.ts";

export function TAAnnouncementPage() {
    const { accessToken } = useAuth();
    const { currentCourseId } = useCurrentCourse();

    const [course, setCourse] = useState<CourseResponse | null>(null);
    const [announcements, setAnnouncements] = useState<Announcement[]>([]);
    const [isPopUpOpen, setIsPopUpOpen] = useState(false);
    const [isLoadingCourse, setIsLoadingCourse] = useState(false);
    const [isLoadingAnnouncements, setIsLoadingAnnouncements] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const sortedAnnouncements = useMemo(
        () =>
            [...announcements].sort(
                (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            ),
        [announcements]
    );

    useEffect(() => {
        if (!currentCourseId || !accessToken) {
            setCourse(null);
            setAnnouncements([]);
            setError(null);
            setIsLoadingCourse(false);
            setIsLoadingAnnouncements(false);
            return;
        }

        let isMounted = true;

        const loadPage = async () => {
            setError(null);
            setIsLoadingCourse(true);
            setIsLoadingAnnouncements(true);

            try {
                const [fetchedCourse, fetchedAnnouncements] = await Promise.all([
                    getCourseById(currentCourseId, accessToken),
                    getAnnouncements(currentCourseId, accessToken),
                ]);

                if (isMounted) {
                    setCourse(fetchedCourse);
                    setAnnouncements(fetchedAnnouncements);
                }
            } catch (loadError) {
                console.error(loadError);
                if (isMounted) {
                    setCourse(null);
                    setAnnouncements([]);
                    setError("Could not load announcements.");
                }
            } finally {
                if (isMounted) {
                    setIsLoadingCourse(false);
                    setIsLoadingAnnouncements(false);
                }
            }
        };

        void loadPage();

        return () => {
            isMounted = false;
        };
    }, [currentCourseId, accessToken]);

    const canCreateAnnouncements = !!course?.canTACreateAnnouncements;
    const shouldShowCreateButton =
        !!currentCourseId && !isLoadingCourse && !isLoadingAnnouncements && canCreateAnnouncements;

    const handleCreateAnnouncement = async (data: CreateAnnouncementRequest) => {
        if (!currentCourseId || !accessToken || !canCreateAnnouncements) {
            throw new Error("Missing permission, course, or access token.");
        }

        const createdAnnouncement = await createAnnouncement(currentCourseId, data, accessToken);
        setAnnouncements((prev) => [createdAnnouncement, ...prev]);
        setIsPopUpOpen(false);
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />
            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">Announcements</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Course announcements overview
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        {!currentCourseId ? (
                            <p className="text-center text-sm text-slate-500">No course selected.</p>
                        ) : (
                            <>
                                <div className="mb-5 flex flex-col items-center gap-3">
                                    {shouldShowCreateButton && (
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setIsPopUpOpen(true);
                                            }}
                                            className="inline-flex w-1/2 items-center justify-center gap-2 rounded-full bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] hover:cursor-pointer"
                                        >
                                            <Plus className="h-4 w-4" />
                                            Create Announcement
                                        </button>
                                    )}

                                    {!isLoadingCourse && course && !canCreateAnnouncements && (
                                        <p className="text-center text-sm text-slate-500">
                                            This course does not allow TAs to create announcements.
                                        </p>
                                    )}
                                </div>

                                {error && (
                                    <p className="mb-4 text-center text-sm text-rose-600">{error}</p>
                                )}

                                <div className="space-y-4">
                                    {isLoadingAnnouncements ? (
                                        <p className="text-center text-sm text-slate-500">
                                            Loading announcements...
                                        </p>
                                    ) : sortedAnnouncements.length === 0 ? (
                                        <p className="text-center text-sm text-slate-500">
                                            No announcements yet.
                                        </p>
                                    ) : (
                                        sortedAnnouncements.map((announcement) => (
                                            <AnnouncementCard
                                                key={announcement.id}
                                                announcement={announcement}
                                            />
                                        ))
                                    )}
                                </div>
                            </>
                        )}
                    </section>
                </div>
            </main>
            <AddAnnouncementPopUp
                isOpen={isPopUpOpen}
                onClose={() => setIsPopUpOpen(false)}
                onCreate={handleCreateAnnouncement}
            />
        </div>
    );
}
