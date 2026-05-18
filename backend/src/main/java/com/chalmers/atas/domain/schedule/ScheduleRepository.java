package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.domain.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    Optional<Schedule> findByCourse(Course course);

    Optional<Schedule> findFirstByCourseCourseId(UUID courseId);
}
