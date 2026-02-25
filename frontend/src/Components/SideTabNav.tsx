import { Link, useLocation, matchPath } from "react-router-dom";
import type { ReactNode } from "react";
import {
    BookOpen,
    Users,
    CalendarDays,
    Megaphone,
    CalendarSync,
    UserCircle2,
    Rows3,
    CirclePlus
} from "lucide-react";

type SidebarItem = {
    id: string;
    label: string;
    to: string;
    pattern?: string;
    icon: ReactNode;
    badgeCount?: number;
};

function SideTabNav() {
    const location = useLocation();

    const items: SidebarItem[] = [
        { id: "add course", label: "Add Course", to: "/addcourse", icon: <CirclePlus size={24} /> },
        { id: "courses", label: "Courses", to: "/courses", icon: <BookOpen size={24} /> },
        { id: "ta list", label: "TA list", to: "/talist", icon: <Users size={24} /> },
        { id: "calendar", label: "Calendar", to: "/calendar", icon: <CalendarDays size={24} /> },
        { id: "constraints", label: "Constraints", to: "/constraints", icon: <Rows3 size={24} /> },
        { id: "announcements", label: "Announcements", to: "/announcements", icon: <Megaphone size={24} /> , badgeCount: 25},
        { id: "requests", label: "Requests", to: "/requests", icon: <CalendarSync size={24} />, badgeCount: 10 },
    ];

    const isActive = (item: SidebarItem) => {
        if (item.pattern) {
            return !!matchPath({ path: item.pattern, end: true }, location.pathname);
        }
        return location.pathname === item.to;
    };

    const isAccountActive = () =>
        location.pathname === "/account" || location.pathname === "/profile";

    return (
        <aside className="fixed left-0 top-0 h-screen w-[104px] bg-[#003b5c] text-white flex flex-col select-none">
            <div className="pt-2">
                <Link
                    to="/"
                    className="w-full flex items-center justify-center px-2 py-2"
                    aria-label="Home"
                >
                    <img
                        src="/SV_Avancez_vit.png"
                        alt="Logo"
                        className="h-[72px] w-[72px] object-contain"
                    />
                </Link>

                <Link
                    to="/account"
                    className={[
                        "relative w-full min-h-[78px] px-2 py-2 flex flex-col items-center justify-center gap-1.5 border-b border-white/10 transition",
                        isAccountActive()
                            ? "bg-white text-[#e85d0c]"
                            : "text-white hover:bg-white/5",
                    ].join(" ")}
                    aria-current={isAccountActive() ? "page" : undefined}
                >
                    {isAccountActive() && (
                        <span className="absolute left-0 top-0 h-full w-[3px] bg-[#e85d0c]" />
                    )}

                    <span className="relative inline-flex items-center justify-center">
            <UserCircle2 size={30} />
          </span>
                    <span className="text-[11px] leading-none font-medium text-center">
            Account
          </span>
                </Link>
            </div>

            <nav className="flex flex-col">
                {items.map((item) => {
                    const active = isActive(item);

                    return (
                        <Link
                            key={item.id}
                            to={item.to}
                            className={[
                                "relative w-full min-h-[78px] px-2 py-2 flex flex-col items-center justify-center gap-1.5 transition",
                                active ? "bg-white text-[#e85d0c]" : "text-white hover:bg-white/5",
                            ].join(" ")}
                            aria-current={active ? "page" : undefined}
                        >
                            {active && (
                                <span className="absolute left-0 top-0 h-full w-[3px] bg-[#e85d0c]" />
                            )}

                            <span className="relative inline-flex items-center justify-center">
                {item.icon}

                                {typeof item.badgeCount === "number" && item.badgeCount > 0 && (
                                    <span className="absolute -top-2 -right-4 min-w-[20px] h-5 px-1.5 rounded-full bg-white text-slate-800 border-2 border-[#003b5c] text-[11px] font-bold leading-none flex items-center justify-center">
                    {item.badgeCount}
                  </span>
                                )}
              </span>

                            <span className="text-[11px] leading-none font-medium text-center">
                {item.label}
              </span>
                        </Link>
                    );
                })}
            </nav>

        </aside>
    );
}

export default SideTabNav;