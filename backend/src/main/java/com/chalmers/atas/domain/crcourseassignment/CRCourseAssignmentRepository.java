package com.chalmers.atas.domain.crcourseassignment;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CRCourseAssignmentRepository extends JpaRepository<CRCourseAssignment, UUID> {
    boolean existsByCrAndCourseAndStatusIn(
            User cr,
            Course course,
            Collection<CourseAssignmentStatus> statuses
    );

    Optional<CRCourseAssignment> findByCrAndCourseAndStatusIn(
            User cr,
            Course course,
            Collection<CourseAssignmentStatus> statuses
    );

    Optional<CRCourseAssignment> findByCrAndCourse(User cr, Course course);

    List<CRCourseAssignment> findAllByCourse(Course course);

    List<CRCourseAssignment> findAllByCr(User user);

    boolean existsByCrAndCourse(User user, Course course);
}
