import { Plus } from "lucide-react";
import SideTabNav from "./SideTabNav";
import { AddAnnouncementPopUp } from "./AddAnnouncementPopUp";
import { AnnouncementCard } from "./AnnouncementCard";
import { useMemo, useState } from "react";

export type Announcement = {
    id: string;
    title: string;
    body: string;
    sendByEmail: boolean;
    createdAt: string
};

//TODO remove when hooked up with backend
const mockData: Announcement[] = [
    {
        id: "1",
        title: "Discord link",
        body: "**We have a Discord server** please join here [Discord Link](https://www.youtube.com/watch?v=iik25wqIuFo&list=RDiik25wqIuFo&start_radio=1)",
        sendByEmail: true,
        createdAt: "2026-03-09T12:12:57Z"
    },
    {
        id: "2",
        title: "Jane Doe",
        body: "[Jane Doe](https://www.youtube.com/watch?v=iNxCLMKGZ0o&list=RDiNxCLMKGZ0o&start_radio=1). [Intro: Hikaru Utada] Mm [Refrain: Hikaru Utada] Marude, kono sekai de Futari dake mitai da ne Nante, sukoshi dake Yume wo mite shimatta dake [Verse 1: Hikaru Utada] Tsumasaki ni tsukiakari Hanataba no kaori Yubi ni fureru yubi Sayonara, mou ikanakya Nani mo kamo Wasurete [Chorus: Hikaru Utada] Garasu no ue wo, hadashi no mama aruku Itamu goto ni chi ga nagarete ochite iku Onegai, sono akai ashiato wo Tadotte ai ni kite [Verse 2: Kenshi Yonezu] Sabita puuru ni hanatarete iku kingyo Kutsubako no naka kakushita ringo Shinabita kimi no hada ni nokoru kizuato Inu no you ni oyoida maigo [Bridge: Kenshi Yonezu, Hikaru Utada, Both] Doko ni iru no (Koko ni iru yo) Nani wo shite iru no (Zutto miteru yo) Kono yo wo machigai de mitasou Soba ni ite yo Asobi ni ikou yo Doko ni iru no [Chorus: Hikaru Utada] Garasu no ue wo hadashi no mama aruku Itamu goto ni chi ga nagarete ochite iku Onegai, sono akai ashiato wo Tadotte ai ni kite [Refrain: Kenshi Yonezu & Hikaru Utada] Marude, kono sekai de (Ooh) Futari dake mitai da ne (Mm, oh, mm) Nante, sukoshi dake (Ooh-ooh, ooh-ooh) Yume wo mite shimatta dakе [Breakdown: Kenshi Yonezu & Hikaru Utada] Eh, oh-eh-ooh, oh-eh-ooh (Ooh) Ah-woah, ah-woah (Eh) La-la-la-la (Oh-ah), la-la-la-la-la-la-la, woah (Eh) Ah (Eh), ah (Eh), ah, mm-mm",
        sendByEmail: false,
        createdAt: "2026-03-09T12:12:54Z"
    }
];

export function CourseResponsibleAnnouncementPage(){
    const [announcements, setAnnouncements] = useState<Announcement[]>(() => [...mockData]);
    const [isPopUpOpen, setIsPopUpOpen] = useState(false);

    const sortedAnnouncements = useMemo(
        () =>
            [...announcements].sort(
                (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            ),
        [announcements]
    );

    const handleCreateAnnouncement = (data: {
        title: string;
        body: string;
        sendByEmail: boolean;
    }) => {
        //TODO replace below, send request to backend, get response, this is just frontend now.
        const created: Announcement = {
            id: crypto.randomUUID(),
            title: data.title,
            body: data.body,
            sendByEmail: data.sendByEmail,
            createdAt: new Date().toISOString()
        };

        setAnnouncements((prev) => [created, ...prev]);
    };


    return (<div className="min-h-screen bg-stone-50">
            <SideTabNav />
            <main className="min-h-screen pl-[104px] py-4">
                <div className="mx-auto w-full max-w-[1500px]">
                    <div className="mb-6 text-center">
                        <h1 className="text-3xl font-bold text-slate-900">Announcements</h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Course responsible Announcements overview
                        </p>
                    </div>

                    <section className="w-full rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                        <div className="mb-5 flex gap-3 sm:flex-row sm:items-center sm:justify-center sm:justify-items-center">
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
                        </div>
                        <div className="space-y-4">
                            {sortedAnnouncements.length === 0 ? (
                                <p className="text-center text-sm text-slate-500">No announcements yet.</p>
                            ) : (
                                sortedAnnouncements.map((announcement) => (
                                    <AnnouncementCard key={announcement.id} announcement={announcement} />
                                ))
                            )}
                        </div>
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
