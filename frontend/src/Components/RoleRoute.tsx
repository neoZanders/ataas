import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export function RoleRoute({
                              allow,
                              children,
                          }: {
    allow: Array<"CR" | "TA">;
    children: React.ReactElement;
}) {
    const { user, isAuthReady } = useAuth();

    if (!isAuthReady) return null;
    if (!user) return <Navigate to="/login" replace />;
    if (!allow.includes(user.userType as any)) return <Navigate to="/unauthorized" replace />;

    return children;
}