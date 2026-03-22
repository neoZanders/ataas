ALTER TABLE courses
    RENAME COLUMN cr_id TO owner_id;

ALTER TABLE courses
    ADD COLUMN description TEXT,
    ADD COLUMN can_ta_see_all_schedules BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN can_ta_create_announcements BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN end_date DATE NOT NULL DEFAULT CURRENT_DATE;

CREATE TABLE cr_course_assignments (
    cr_course_assignment_id UUID PRIMARY KEY,
    cr_user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT chk_cr_course_assignments_status
        CHECK (status IN ('OWNER', 'INVITED', 'JOINED')),

    CONSTRAINT fk_cr_course_assignments_user
        FOREIGN KEY (cr_user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cr_course_assignments_course
        FOREIGN KEY (course_id)
            REFERENCES courses(course_id)
            ON DELETE CASCADE,

    CONSTRAINT uq_cr_course_assignments_user_course
        UNIQUE (cr_user_id, course_id)
);
