package com.chalmers.atas.domain.tacourseassignment;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.user.User;


public interface TACourseAssignmentRepository extends JpaRepository<TACourseAssignment, UUID>{
    Optional<TACourseAssignment> findByTaAndCourse(User ta, Course course);

    boolean existsByTaAndCourseAndStatus(User ta, Course course, CourseAssignmentStatus status);

    List<TACourseAssignment> findAllByCourse(Course course, Sort sort);

    List<TACourseAssignment> findAllByTa(User ta);

    List<TACourseAssignment> findAllByCourseAndTaName(Course course, String username, Sort sort);
}
