export const TOKEN_KEY = "mtb_access_token";
export const USER_KEY = "mtb_user";

export function getStoredAccessToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
}

export function setStoredAccessToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredAuth(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}