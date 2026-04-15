import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { CourseResponse } from "../api/coursesApi.ts";

type CurrentCourseContextType = {
    currentCourseId: string | null;
    setCurrentCourseId: (courseId: string | null) => void;
    currentCourse: CourseResponse | null;
    setCurrentCourse: (course: CourseResponse | null) => void;
};

const CurrentCourseContext = createContext<CurrentCourseContextType | undefined>(undefined);

const CURRENT_COURSE_ID_KEY = "currentCourseId";

export function CurrentCourseProvider({ children }: { children: ReactNode }) {
    const [currentCourseId, setCurrentCourseIdState] = useState<string | null>(null);
    const [currentCourse, setCurrentCourse] = useState<CourseResponse | null>(null);

    useEffect(() => {
        const savedCourseId = localStorage.getItem(CURRENT_COURSE_ID_KEY);
        if (savedCourseId) {
            setCurrentCourseIdState(savedCourseId);
        }
    }, []);

    const setCurrentCourseId = (courseId: string | null) => {
        setCurrentCourseIdState(courseId);

        if (courseId) {
            localStorage.setItem(CURRENT_COURSE_ID_KEY, courseId);
        } else {
            localStorage.removeItem(CURRENT_COURSE_ID_KEY);
        }
    };

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