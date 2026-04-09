package com.chalmers.atas.domain.courseassignment;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseAuthorizationService {

    private final CourseService courseService;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final TACourseAssignmentService taCourseAssignmentService;

    public Result<Course> assertUserIsCrOfCourse(UUID courseId, User user) {
        return courseService.getCourse(courseId).flatMap(course -> {
            if (!crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
            }
            return Result.ok(course);
        });
    }

    public Result<Course> assertUserIsTaOfCourse(UUID courseId, User user) {
        return courseService.getCourse(courseId).flatMap(course -> {
            if (!taCourseAssignmentService.isUserTaOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError());
            }
            return Result.ok(course);
        });
    }
}
