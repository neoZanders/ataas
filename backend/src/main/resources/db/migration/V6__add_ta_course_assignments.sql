CREATE TABLE ta_course_assignments (
    ta_course_assignment_id UUID PRIMARY KEY,
    ta_user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    min_hours INTEGER NULL,
    max_hours INTEGER NULL,
    session_type_preference_1 VARCHAR(20) NULL,
    session_type_preference_2 VARCHAR(20) NULL,
    session_type_preference_3 VARCHAR(20) NULL,
    session_type_preference_4 VARCHAR(20) NULL,
    is_compact_schedule BOOLEAN NULL,

    CONSTRAINT chk_ta_course_assignments_status
        CHECK (status IN ('INVITED', 'JOINED')),

    CONSTRAINT fk_ta_course_assignments_user
        FOREIGN KEY (ta_user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ta_course_assignments_course
        FOREIGN KEY (course_id)
            REFERENCES courses(course_id)
            ON DELETE CASCADE,

    CONSTRAINT uq_ta_course_assignments_user_course
        UNIQUE (ta_user_id, course_id),

    CONSTRAINT chk_ta_course_assignments_session_type_preferences
        CHECK (
            (session_type_preference_1 IN ('GRADING','LABORATION','HELP','EXERCISE') OR session_type_preference_1 IS NULL)
            AND (session_type_preference_2 IN ('GRADING','LABORATION','HELP','EXERCISE') OR session_type_preference_2 IS NULL)
            AND (session_type_preference_3 IN ('GRADING','LABORATION','HELP','EXERCISE') OR session_type_preference_3 IS NULL)
            AND (session_type_preference_4 IN ('GRADING','LABORATION','HELP','EXERCISE') OR session_type_preference_4 IS NULL)
        )
);
