package com.chalmers.atas.api.course;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseService courseService;

    public Result<CourseResponse> createCourse(CreateCourseRequest request, CurrentUser currentUser) {
        return courseService.createCourse(request.getCourseCode(), currentUser.getUser()).map(CourseResponse::of);
    }

    public Result<List<CourseResponse>> getCourses(CurrentUser currentUser) {
        if (currentUser.getUser().getUserType().equals(User.UserType.CR)) {
            return courseService.getCourses(currentUser.getUser())
                    .map(courses ->
                            courses.stream().map(CourseResponse::of).toList());
        } else {
            throw new RuntimeException("Not implemented for TA yet.");
        }
    }

    public Result<CourseResponse> archiveCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.archiveCourse(courseId, currentUser.getUser()).map(CourseResponse::of);
    }

    public Result<Void> deleteCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.deleteCourse(courseId, currentUser.getUser());
    }
}
