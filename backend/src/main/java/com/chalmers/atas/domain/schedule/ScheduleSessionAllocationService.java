package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleSessionAllocationService {

    private final CourseRepository courseRepository;
    private final ScheduleSessionAllocationRepository scheduleSessionAllocationRepository;

    public Result<List<ScheduleSessionAllocation>> getAllocations(UUID courseId, User user) {
        return getCourseIfOwnedByCr(courseId, user)
                .map(scheduleSessionAllocationRepository::findByCourse);
    }

    @Transactional
    public TransactionalResult<List<ScheduleSessionAllocation>> replaceAllocations(
            UUID courseId,
            List<ScheduleSessionAllocation> allocations,
            User user
    ) {
        Result<Course> maybeCourse = getCourseIfOwnedByCr(courseId, user);
        if (!maybeCourse.isSuccess()) {
            return TransactionalResult.rollbackFor(maybeCourse.getError());
        }

        Course course = maybeCourse.getData();
        boolean mismatchedCourse = allocations.stream()
                .anyMatch(allocation -> !allocation.getCourse().getCourseId().equals(course.getCourseId()));
        if (mismatchedCourse) {
            return TransactionalResult.rollbackFor(ErrorCode.BAD_REQUEST.toError(
                    "All allocations must belong to the requested course"));
        }

        scheduleSessionAllocationRepository.deleteByCourse(course);
        return TransactionalResult.ok(scheduleSessionAllocationRepository.saveAll(allocations));
    }

    private Result<Course> getCourseIfOwnedByCr(UUID courseId, User user) {
        Optional<Course> maybeCourse = courseRepository.findById(courseId);
        if (maybeCourse.isEmpty()) {
            return Result.error(ErrorCode.COURSE_NOT_FOUND.toError());
        }

        Course course = maybeCourse.get();
        if (!course.getCr().getUserId().equals(user.getUserId())) {
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
        }

        return Result.ok(course);
    }
}
