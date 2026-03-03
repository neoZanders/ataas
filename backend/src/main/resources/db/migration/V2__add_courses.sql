CREATE TABLE courses (
    course_id UUID PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    cr_id UUID NOT NULL,
    course_status VARCHAR(20) NOT NULL,

    CONSTRAINT chk_courses_course_status
        CHECK (course_status IN ('ACTIVE', 'ARCHIVED')),

    CONSTRAINT fk_courses_cr
        FOREIGN KEY (cr_id)
            REFERENCES users(user_id)
);