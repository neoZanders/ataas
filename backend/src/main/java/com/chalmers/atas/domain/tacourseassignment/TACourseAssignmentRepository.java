package com.chalmers.atas.domain.tacourseassignment;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;


public interface TACourseAssignmentRepository extends JpaRepository<TACourseAssignment, UUID>{
    Optional<TACourseAssignment> findByTaAndCourse(User ta, Course course);

    boolean existsByTaAndCourse(User ta, Course course);

    List<TACourseAssignment> findAllByCourse(Course course);

    List<TACourseAssignment> findAllByTa(User ta);
}
