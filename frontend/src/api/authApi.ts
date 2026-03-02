import { fetchJson } from "./http";

export type UserResponse = {
    userId: string;
    email: string;
    name: string;
    userType: "CR" | "TA" | string;
}

export type RegisterRequest = {
    email: string;
    name: string;
    password: string;
    userType: "CR" | "TA" | string;
};

export type AuthResponse = {
    userResponse: UserResponse;
    accessToken: string;
};

export type LoginRequest = {
    email: string;
    password: string;
};

export async function register(req: RegisterRequest): Promise<AuthResponse> {
    return fetchJson<AuthResponse>(`/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
    });
}

export async function login(req: LoginRequest): Promise<AuthResponse> {
    return fetchJson<AuthResponse>(`/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
    });
}