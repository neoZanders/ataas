import { createContext, useContext, useState, type ReactNode } from "react";
import type { CourseResponse } from "../api/coursesApi.ts";

type CurrentCourseContextType = {
    currentCourseId: string | null;
    setCurrentCourseId: (courseId: string | null) => void;
    currentCourse: CourseResponse | null;
    setCurrentCourse: (course: CourseResponse | null) => void;
};

const CurrentCourseContext = createContext<CurrentCourseContextType | undefined>(undefined);

export function CurrentCourseProvider({ children }: { children: ReactNode }) {
    const [currentCourseId, setCurrentCourseId] = useState<string | null>(null);
    const [currentCourse, setCurrentCourse] = useState<CourseResponse | null>(null);

    return (
        <CurrentCourseContext.Provider
            value={{
                currentCourseId,
                setCurrentCourseId,
                currentCourse,
                setCurrentCourse,
            }}
        >
            {children}
        </CurrentCourseContext.Provider>
    );
}

export function useCurrentCourse() {
    const context = useContext(CurrentCourseContext);
    if (!context) {
        throw new Error("useCurrentCourse must be used within CurrentCourseProvider");
    }
    return context;
}