import Calendar from "./Calendar";
import SideTabNav from "./SideTabNav.tsx";

export function CourseResponsibleMainPage() {
    return (
        <div className="min-h-screen bg-stone-50">
            <SideTabNav />
            <main className="pl-[104px] pt-6">
                <div className="flex items-center justify-center mb-6">
                <button
                    className="rounded-2xl bg-[#003b5c] text-xl font-medium text-slate-50 hover:bg-[#002741] px-10 py-2"
                    type="button">
                    Run Algorithm
                </button>
                </div>
                <Calendar />
            </main>
        </div>
    );
}