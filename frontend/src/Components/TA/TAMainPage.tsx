import SideTabNav from "../SideTabNav.tsx";
import Calendar from "../Calendar.tsx";

export function TAMainPage(){
    return (
        <div className="flex items-center justify-center px-2 py-2">
            <SideTabNav />
            <main className="pl-[104px] pt-6">
            <Calendar />
            </main>
        </div>

    )
}