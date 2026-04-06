package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TACourseSessionConstraintRepository extends JpaRepository<TACourseSessionConstraint, UUID> {
    List<TACourseSessionConstraint> findAllByTaCourseAssignment_Course_CourseId(UUID courseId);

    List<TACourseSessionConstraint> findAllByTaCourseAssignment_Ta_UserIdAndTaCourseAssignment_Course_CourseId(
            UUID taId,
            UUID courseId
    );
}
