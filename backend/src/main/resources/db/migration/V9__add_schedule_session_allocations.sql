CREATE TABLE schedule_session_allocations (
    schedule_session_allocations_id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL,
    course_session_id UUID NOT NULL,
    ta_course_assignment_id UUID NOT NULL,

    CONSTRAINT fk_schedule_allocations_course
        FOREIGN KEY (schedule_id)
            REFERENCES schedules(schedule_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_schedule_session_allocations_course_session
        FOREIGN KEY (course_session_id)
            REFERENCES course_sessions(course_session_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_schedule_session_allocations_ta_course_assignment
        FOREIGN KEY (ta_course_assignment_id)
            REFERENCES ta_course_assignments(ta_course_assignment_id)
            ON DELETE CASCADE
);