CREATE TABLE ta_course_session_constraints (
    ta_course_session_constraint_id UUID PRIMARY KEY,
    ta_course_assignment_id UUID NOT NULL,
    constraint_type VARCHAR(20) NOT NULL,
    start_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_weekly_recurring BOOLEAN NOT NULL,

    CONSTRAINT chk_ta_course_session_constraints_type
        CHECK (constraint_type IN ('SOFT', 'HARD')),

    CONSTRAINT fk_ta_course_session_constraints_assignment
        FOREIGN KEY (ta_course_assignment_id)
            REFERENCES ta_course_assignments(ta_course_assignment_id)
            ON DELETE CASCADE
);
