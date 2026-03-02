import { useNavigate } from "react-router-dom";
import { LogOut, UserCircle2, Mail, Shield } from "lucide-react";
import SideTabNav from "./SideTabNav";
import { useAuth } from "./AuthContext";

export function ProfilePage() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />

            <main className="min-h-screen ml-[104px] flex items-center justify-center">
                <div className="mx-auto w-full max-w-3xl">
                    <div className="rounded-3xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
                        <div className="mb-8 flex items-center gap-4">
                            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-[#003b5c]/10 text-[#003b5c]">
                                <UserCircle2 className="h-10 w-10" />
                            </div>

                            <div>
                                <h1 className="text-2xl font-semibold text-slate-900">Profile</h1>
                                <p className="text-sm text-slate-500">
                                    Manage your account information.
                                </p>
                            </div>
                        </div>

                        <div className="space-y-4">
                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <div className="mb-1 flex items-center gap-2 text-sm font-medium text-slate-600">
                                    <UserCircle2 className="h-4 w-4" />
                                    Name
                                </div>
                                <p className="text-base font-semibold text-slate-900">
                                    {user?.name ?? "Unknown user"}
                                </p>
                            </div>

                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <div className="mb-1 flex items-center gap-2 text-sm font-medium text-slate-600">
                                    <Mail className="h-4 w-4" />
                                    Email
                                </div>
                                <p className="text-base font-semibold text-slate-900">
                                    {user?.email ?? "No email"}
                                </p>
                            </div>

                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                <div className="mb-1 flex items-center gap-2 text-sm font-medium text-slate-600">
                                    <Shield className="h-4 w-4" />
                                    Role
                                </div>
                                <p className="text-base font-semibold text-slate-900">
                                    {user?.userType === "CR" ? "Course Responsible" : "Teaching Assistant"}
                                </p>
                            </div>
                        </div>

                        <div className="mt-8 flex justify-end">
                            <button
                                type="button"
                                onClick={handleLogout}
                                className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[#003b5c] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#002f49]"
                            >
                                <LogOut className="h-4 w-4" />
                                Log out
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}