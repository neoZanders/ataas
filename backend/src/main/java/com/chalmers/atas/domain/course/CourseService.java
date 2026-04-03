package com.chalmers.atas.domain.course;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final static String courseCodeMatcher = "^[A-Za-z]{3}\\d{3}$";

    private final CourseRepository courseRepository;

    @Transactional
    public TransactionalResult<Course> createCourse(
            User owner,
            String courseCode,
            String description,
            boolean canTASeeAllSchedules,
            boolean canTACreateAnnouncements,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (!courseCode.matches(courseCodeMatcher)) {
            return TransactionalResult.rollbackFor(ErrorCode.INVALID_COURSE_CODE.toError());
        }

        if (startDate.isAfter(endDate)) {
            return TransactionalResult.rollbackFor(ErrorCode.START_AFTER_END.toError());
        }

        return TransactionalResult.ok(courseRepository.save(
                Course.of(
                        courseCode,
                        owner,
                        description,
                        canTASeeAllSchedules,
                        canTACreateAnnouncements,
                        startDate,
                        endDate
                )));
    }

    @Transactional
    public TransactionalResult<Course> archiveCourse(UUID courseId, User user) {
        return TransactionalResult.from(
                getCourseIfOwnedByCr(courseId, user)
                        .peek(course -> course.setCourseStatus(Course.CourseStatus.ARCHIVED)));
    }

    @Transactional
    public TransactionalResult<Void> deleteCourse(UUID courseId, User user) {
        return TransactionalResult.from(
                getCourseIfOwnedByCr(courseId, user).then(courseRepository::delete));
    }

    @Transactional
    public TransactionalResult<Course> updateCourse(
            UUID courseId,
            User user,
            String description,
            boolean canTASeeAllSchedules,
            boolean canTACreateAnnouncements
    ) {
        return TransactionalResult.from(getCourseIfOwnedByCr(courseId, user).map(course -> {
            course.setDescription(description);
            course.setCanTASeeAllSchedules(canTASeeAllSchedules);
            course.setCanTACreateAnnouncements(canTACreateAnnouncements);
            return courseRepository.save(course);
        }));
    }

    private Result<Course> getCourseIfOwnedByCr(UUID courseId, User user) {
        Optional<Course> maybeCourse = courseRepository.findById(courseId);
        if (maybeCourse.isEmpty()) {
            return Result.error(ErrorCode.COURSE_NOT_FOUND.toError());
        }

        Course course = maybeCourse.get();

        if (!course.getOwner().getUserId().equals(user.getUserId())) {
            return Result.error(
                    ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError()
            );
        }
        return Result.ok(course);
    }

    public Result<Course> getCourse(UUID courseId) {
        Optional<Course> maybeCourse = courseRepository.findById(courseId);
        if (maybeCourse.isEmpty()) {
            return Result.error(ErrorCode.COURSE_NOT_FOUND.toError());
        }
        return Result.ok(maybeCourse.get());
    }
}
