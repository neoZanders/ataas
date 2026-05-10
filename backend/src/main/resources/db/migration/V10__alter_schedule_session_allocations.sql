-- noinspection SqlResolveForFile
ALTER TABLE schedule_session_allocations
    DROP CONSTRAINT fk_schedule_session_allocations_course_session;

-- noinspection SqlResolveForFile
ALTER TABLE schedule_session_allocations
    DROP COLUMN course_session_id;

ALTER TABLE schedule_session_allocations
    ADD COLUMN start_date_time TIMESTAMP NOT NULL,
    ADD COLUMN end_date_time TIMESTAMP NOT NULL,
    ADD COLUMN course_session_type VARCHAR(20) NOT NULL;

-- noinspection SqlResolveForFile
ALTER TABLE schedule_session_allocations
    RENAME COLUMN schedule_session_allocations_id TO schedule_session_allocation_id;