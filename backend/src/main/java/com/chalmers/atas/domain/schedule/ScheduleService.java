package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAuthorizationService;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.chalmers.atas.common.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CourseAuthorizationService courseAuthorizationService;
    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public TransactionalResult<Schedule> createSchedule(UUID courseId, User user) {
        Result<Course> maybeCourse = getCourseIfOwnedByCr(courseId, user);
        if (!maybeCourse.isSuccess()) {
            return TransactionalResult.rollbackFor(maybeCourse.getError());
        }

        Course course = maybeCourse.getData();
        return TransactionalResult.ok(
                scheduleRepository.findFirstByCourse(course)
                        .orElseGet(() -> scheduleRepository.save(Schedule.of(course)))
        );
    }

    public Result<List<Schedule>> getSchedule(UUID courseId, User user) {
        return getCourseIfUserCanViewSchedule(courseId, user)
                .map(scheduleRepository::findByCourse);
    }

    private Result<Course> getCourseIfUserCanViewSchedule(UUID courseId, User user) {
        if (user.getUserType().equals(User.UserType.CR)) {
            return courseAuthorizationService.assertUserIsCrOfCourse(courseId, user);
        }
        if (user.getUserType().equals(User.UserType.TA)) {
            return courseAuthorizationService.assertUserIsTaOfCourse(courseId, user);
        }
        return Result.errorFromCode(USER_NOT_ALLOWED_FOR_COURSE_ACTION);
    }

    private Result<Course> getCourseIfOwnedByCr(UUID courseId, User user) {
        Optional<Course> maybeCourse = courseRepository.findById(courseId);
        if (maybeCourse.isEmpty()) {
            return Result.errorFromCode(COURSE_NOT_FOUND);
        }

        Course course = maybeCourse.get();
        if (!course.getOwner().getUserId().equals(user.getUserId())) {
            return Result.errorFromCode(USER_NOT_COURSE_RESPONSIBLE);
        }

        return Result.ok(course);
    }
}
