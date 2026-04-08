import type { UserResponse } from "../api/authApi.ts";

export type Announcement = {
    id: string;
    owner: UserResponse;
    title: string;
    body: string;
    sendByEmail: boolean;
    createdAt: string;
};

export type CreateAnnouncementRequest = {
    title: string;
    body: string;
    sendByEmail: boolean;
};
