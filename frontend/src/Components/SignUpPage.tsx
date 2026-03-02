import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Mail, LockKeyhole, User, UserPlus } from "lucide-react";
import { useAuth } from "./AuthContext";
import { ApiError } from "../api/http";

export function SignUpPage() {
    const { register } = useAuth();
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isCourseResponsible, setIsCourseResponsible] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            await register({
                name,
                email,
                password,
                userType: isCourseResponsible ? "CR" : "TA",
            });

            navigate("/calendar");
        } catch (err) {
            if (err instanceof ApiError) {
                const msg =
                    (err.body as any)?.message ||
                    (err.body as any)?.error ||
                    `Sign up failed (HTTP ${err.status}).`;
                setError(msg);
            } else {
                setError("Sign up failed. Please try again.");
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
                        <UserPlus className="h-7 w-7" />
                    </div>

                    <h1 className="text-2xl font-semibold text-slate-900">Sign up</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Create an account to access your course workspace.
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
                            Name
                        </label>
                        <div className="relative">
              <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <User className="h-4 w-4" />
              </span>
                            <input
                                type="text"
                                autoComplete="name"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="w-full rounded-2xl border border-slate-300 bg-white py-3 pl-10 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                placeholder="Your name"
                            />
                        </div>
                    </div>

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
                                autoComplete="new-password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full rounded-2xl border border-slate-300 bg-white py-3 pl-10 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#003b5c]"
                                placeholder="••••••••"
                            />
                        </div>
                    </div>

                    <label className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                        <input
                            type="checkbox"
                            checked={isCourseResponsible}
                            onChange={(e) => setIsCourseResponsible(e.target.checked)}
                            className="h-4 w-4 rounded border-slate-300 accent-[#003b5c]"
                        />
                        <span>Sign up as course responsible</span>
                    </label>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[#003b5c] py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49] disabled:cursor-not-allowed disabled:opacity-70"
                    >
                        <UserPlus className="h-4 w-4" />
                        {isSubmitting ? "Signing up..." : "Sign up"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-slate-600">
                    Already have an account?{" "}
                    <Link
                        to="/login"
                        className="font-semibold text-[#003b5c] hover:underline"
                    >
                        Log in
                    </Link>
                </p>
            </div>
        </div>
    );
}