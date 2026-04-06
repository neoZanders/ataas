import { fetchJson, ApiError } from "./http";

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

    return fetchJson<T>(path, {
        ...options,
        headers,
    });
}
