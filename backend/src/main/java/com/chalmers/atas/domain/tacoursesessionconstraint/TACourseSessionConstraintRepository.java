package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chalmers.atas.domain.course.Course;

public interface TACourseSessionConstraintRepository extends JpaRepository<TACourseSessionConstraint, UUID> {
    List<TACourseSessionConstraint> findAllByTaCourseAssignment_Course(Course course);

    List<TACourseSessionConstraint> findAllByTaCourseAssignment_Ta_UserIdAndTaCourseAssignment_Course(
            UUID taId,
            Course course
    );
}
