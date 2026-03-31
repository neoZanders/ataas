package com.chalmers.atas.domain.coursesession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseSessionRepository extends JpaRepository<CourseSession, UUID> {
    List<CourseSession> findAllByCourseCourseId(UUID courseId);
}
