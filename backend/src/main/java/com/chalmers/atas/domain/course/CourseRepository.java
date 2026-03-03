package com.chalmers.atas.domain.course;

import com.chalmers.atas.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByCr(User cr);
}
