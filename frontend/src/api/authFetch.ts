import { fetchJson, ApiError } from "./http";
import * as authApi from "./authApi";
import {clearStoredAuth, setStoredAccessToken} from "./authStorage.ts";

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
    if (!refreshPromise) {
        refreshPromise = authApi
            .refresh()
            .then((res) => {
                setStoredAccessToken(res.accessToken);
                return res.accessToken;
            })
            .catch(() => {
                clearStoredAuth()
                return null;
            })
        .finally(() => {
            refreshPromise = null;
        })
    }
    return refreshPromise
}

export async function authFetchJson<T>(
    path: string,
    accessToken: string | null,
    options: RequestInit = {}
): Promise<T> {
    if (!accessToken) {
        throw new ApiError("Missing access token", 401, null);
    }

    const headers: Record<string, string> = {
        ...(options.headers as Record<string, string> | undefined),
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
    };

    try {
        return await fetchJson<T>(path, {
            ...options,
            headers,
        });
    } catch (error) {
        if (!(error instanceof ApiError)) {
            throw error;
        }
        if (error.status !== 401) {
            throw error;
        }

        const newAccessToken = await refreshAccessToken();

        if (!newAccessToken) {
            throw new ApiError("session expired", 401, null);
        }

        const retryHeaders: Record<string, string> = {
            ...(options.headers as Record<string, string> | undefined),
            Authorization: `Bearer ${newAccessToken}`,
            "Content-Type": "application/json",
        }

        return fetchJson<T>(path, {
            ...options,
            headers: retryHeaders,
        });
    }
}
