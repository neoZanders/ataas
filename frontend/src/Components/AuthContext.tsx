import React, { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/authApi";

type AuthContextValue = {
    user: User | null;
    accessToken: string | null;
    isAuthReady: boolean;
    register: (payload: authApi.RegisterRequest) => Promise<void>;
    login: (payload: authApi.LoginRequest) => Promise<void>;
    logout: () => void;
};

export type User = {
    id: string;
    name: string;
    email: string;
    userType: "CR" | "TA" | string;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = "mtb_access_token";
const USER_KEY = "mtb_user";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isAuthReady, setIsAuthReady] = useState(false);

    useEffect(() => {
        const savedToken = localStorage.getItem(TOKEN_KEY);
        const savedUser = localStorage.getItem(USER_KEY);

        // eslint-disable-next-line react-hooks/set-state-in-effect
        if (savedToken) setAccessToken(savedToken);

        if (savedUser) {
            try {
                setUser(JSON.parse(savedUser));
            } catch {
                localStorage.removeItem(USER_KEY);
            }
        }

        setIsAuthReady(true);
    }, []);

    const mapUser = (userResponse: authApi.UserResponse): User => {
        return {
            id: userResponse.userId,
            email: userResponse.email,
            name: userResponse.name,
            userType: userResponse.userType,
        };
    };

    const register = async (payload: authApi.RegisterRequest) => {
        const res = await authApi.register(payload);

        const mappedUser = mapUser(res.userResponse);

        setUser(mappedUser);
        setAccessToken(res.accessToken);

        localStorage.setItem(TOKEN_KEY, res.accessToken);
        localStorage.setItem(USER_KEY, JSON.stringify(mappedUser));
    };

    const login = async (payload: authApi.LoginRequest) => {
        const res = await authApi.login(payload);

        const mappedUser = mapUser(res.userResponse);

        setUser(mappedUser);
        setAccessToken(res.accessToken);

        localStorage.setItem(TOKEN_KEY, res.accessToken);
        localStorage.setItem(USER_KEY, JSON.stringify(mappedUser));
    };

    const logout = () => {
        setUser(null);
        setAccessToken(null);
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    };

    return (
        <AuthContext.Provider value={{ user, accessToken, isAuthReady, register, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
    return ctx;
}