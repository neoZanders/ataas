import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import type { EventInput } from "@fullcalendar/core";

const mockEvents: EventInput[] = [
    { id: "1", title: "Session: Data science", start: "2026-02-24T10:00:00", end: "2026-02-24T11:00:00" },
    { id: "2", title: "Session: Digital design", start: "2026-02-26T10:00:00", end: "2026-02-26T11:00:00" },
    { id: "3", title: "TA Planning", start: "2026-02-27T13:00:00", end: "2026-02-27T14:30:00" },
];

export default function Calendar() {

    return (
        <div className="calendar-theme rounded-2xl bg-white p-4 shadow-sm ring-1 ring-black/5">
            <FullCalendar
                plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                headerToolbar={{
                    left: "prev,next today",
                    center: "title",
                    right: "dayGridMonth,timeGridWeek,timeGridDay",
                }}
                buttonText={{
                    today: "Today",
                    month: "Month",
                    week: "Week",
                    day: "Day",
                }}
                events={mockEvents}
                selectable
                editable={false}
                dayMaxEvents={true}
                weekends={true}
                height="auto"
            />
        </div>
    );
}