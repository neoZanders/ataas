import React, { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/authApi";
import {
    TOKEN_KEY,
    USER_KEY,
    setStoredAccessToken,
    clearStoredAuth,
} from "../api/authStorage";

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

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isAuthReady, setIsAuthReady] = useState(false);

    useEffect(() => {
        const savedToken = localStorage.getItem(TOKEN_KEY);
        const savedUser = localStorage.getItem(USER_KEY);

        if (savedToken) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setAccessToken(savedToken);
        }

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

    const saveAuthData = (mappedUser: User, token: string) => {
        setUser(mappedUser);
        setAccessToken(token);
        setStoredAccessToken(token);
        localStorage.setItem(USER_KEY, JSON.stringify(mappedUser));
    }

    const register = async (payload: authApi.RegisterRequest) => {
        const res = await authApi.register(payload);
        const mappedUser = mapUser(res.userResponse);

        saveAuthData(mappedUser, res.accessToken);
    };

    const login = async (payload: authApi.LoginRequest) => {
        const res = await authApi.login(payload);
        const mappedUser = mapUser(res.userResponse);

        saveAuthData(mappedUser, res.accessToken);
    };

    const logout = () => {
        setUser(null);
        setAccessToken(null);
        clearStoredAuth();
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