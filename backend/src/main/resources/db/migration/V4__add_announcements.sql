CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    announcement TEXT NOT NULL,
    send_by_email BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_announcements_course
        FOREIGN KEY (course_id)
            REFERENCES courses(course_id)
            ON DELETE CASCADE,
    
    CONSTRAINT fk_announcements_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(user_id)
);
