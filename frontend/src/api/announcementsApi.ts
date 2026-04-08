import { authFetchJson } from "./authFetch.ts";
import type { Announcement, CreateAnnouncementRequest } from "../types/announcement.ts";

export async function getAnnouncements(
    courseId: string,
    accessToken: string | null
): Promise<Announcement[]> {
    return authFetchJson<Announcement[]>(`/api/courses/${courseId}/announcements`, accessToken, {
        method: "GET",
    });
}

export async function createAnnouncement(
    courseId: string,
    request: CreateAnnouncementRequest,
    accessToken: string | null
): Promise<Announcement> {
    return authFetchJson<Announcement>(`/api/courses/${courseId}/announcements`, accessToken, {
        method: "POST",
        body: JSON.stringify(request),
    });
}
