CREATE TABLE schedule_session_allocations (
    allocation_id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    session_id UUID NOT NULL,
    ta_id UUID NOT NULL,

    CONSTRAINT fk_schedule_allocations_course
        FOREIGN KEY (course_id)
            REFERENCES courses(course_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_schedule_allocations_ta
        FOREIGN KEY (ta_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);