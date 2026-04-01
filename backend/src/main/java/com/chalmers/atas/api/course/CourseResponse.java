package com.chalmers.atas.api.course;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.course.Course;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseResponse {

    private UUID courseId;

    private String courseCode;

    private UserResponse owner;

    private String description;

    private Course.CourseStatus status;

    private boolean canTASeeAllSchedules;

    private boolean canTACreateAnnouncements;

    private LocalDate startDate;

    private LocalDate endDate;

    public static CourseResponse of(Course course) {
        return new CourseResponse(
                course.getCourseId(),
                course.getCourseCode(),
                UserResponse.of(course.getOwner()),
                course.getDescription(),
                course.getCourseStatus(),
                course.isCanTASeeAllSchedules(),
                course.isCanTACreateAnnouncements(),
                course.getStartDate(),
                course.getEndDate()
        );
    }
}
