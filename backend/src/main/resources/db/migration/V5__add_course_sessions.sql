CREATE TABLE course_sessions(
    course_session_id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    start_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    course_session_type VARCHAR(20) NOT NULL,
    min_tas INTEGER NOT NULL,
    max_tas INTEGER NOT NULL,
    is_weekly_recurring BOOLEAN NOT NULL

    CONSTRAINT chk_course_sessions_course_session_type
        CHECK (course_session_type IN (
            'GRADING',
            'LABORATION',
            'HELP',
            'EXERCISE'
        )
    ),

    CONSTRAINT fk_course_sessions_course
        FOREIGN KEY (course_id)
            REFERENCES courses(course_id)
            ON DELETE CASCADE
)