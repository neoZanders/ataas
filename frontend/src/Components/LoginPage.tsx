import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Mail, LockKeyhole, LogIn } from "lucide-react";
import { useAuth } from "./AuthContext";
import { ApiError } from "../api/http";

export function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            await login({ email, password });
            navigate("/");
        } catch (err) {
            if (err instanceof ApiError) {
                const msg =
                    (err.body as any)?.message ||
                    (err.body as any)?.error ||
                    `Login failed (HTTP ${err.status}).`;
                setError(msg);
            } else {
                setError("Login failed. Please try again.");
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-stone-50 px-4">
            <div className="w-full max-w-md rounded-3xl bg-white p-8 shadow-xl ring-1 ring-slate-200">
                <div className="mb-6 text-center">
                    <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#003b5c]/10 text-[#003b5c]">
                        <LogIn className="h-7 w-7" />
                    </div>

                    <h1 className="text-2xl font-semibold text-slate-900">Log in</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Log in to continue to your course workspace.
                    </p>
                </div>

                {error && (
                    <div className="mb-4 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Email
                        </label>
                        <div className="relative">
              <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <Mail className="h-4 w-4" />
              </span>
                            <input
                                type="email"
                                autoComplete="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full rounded-2xl border border-slate-300 bg-white py-3 pl-10 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                placeholder="cid@chalmers.se"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-slate-700">
                            Password
                        </label>
                        <div className="relative">
              <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <LockKeyhole className="h-4 w-4" />
              </span>
                            <input
                                type="password"
                                autoComplete="current-password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full rounded-2xl border border-slate-300 bg-white py-3 pl-10 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                placeholder="••••••••"
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[#003b5c] py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:cursor-not-allowed disabled:opacity-70"
                    >
                        <LogIn className="h-4 w-4" />
                        {isSubmitting ? "Logging in..." : "Log in"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-slate-600">
                    Don&apos;t have an account?{" "}
                    <Link
                        to="/signup"
                        className="font-semibold text-[#003b5c] hover:underline"
                    >
                        Sign up
                    </Link>
                </p>
            </div>
        </div>
    );
}