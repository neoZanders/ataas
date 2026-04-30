package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chalmers.atas.domain.course.Course;

public interface TACourseSessionConstraintRepository extends JpaRepository<TACourseSessionConstraint, UUID> {
    List<TACourseSessionConstraint> findAllByTaCourseAssignmentCourse(Course course);

    List<TACourseSessionConstraint> findAllByTaCourseAssignmentTaUserIdAndTaCourseAssignmentCourseOrderByStartDateTime(
            UUID taId,
            Course course
    );

    List<TACourseSessionConstraint> findAllByTaCourseAssignmentCourseAndTaCourseAssignmentTaNameOrderByStartDateTime(Course course, String username);
}
