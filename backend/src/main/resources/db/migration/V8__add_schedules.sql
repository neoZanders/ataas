CREATE TABLE schedules (
                           schedule_id UUID PRIMARY KEY,
                           course_id UUID NOT NULL,

                           CONSTRAINT fk_schedules_course
                               FOREIGN KEY (course_id)
                                   REFERENCES courses(course_id)
                                   ON DELETE CASCADE
);