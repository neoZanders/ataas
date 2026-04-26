package com.chalmers.atas.domain.schedulesessionallocation;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAuthorizationService;
import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.schedule.ScheduleRepository;
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

    private final CourseAuthorizationService courseAuthorizationService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSessionAllocationRepository scheduleSessionAllocationRepository;

    public Result<List<ScheduleSessionAllocation>> getAllocations(UUID courseId, User user) {
        return getScheduleIfUserCanViewSchedule(courseId, user)
                .map(scheduleSessionAllocationRepository::findBySchedule);
    }

    @Transactional
    public TransactionalResult<List<ScheduleSessionAllocation>> replaceAllocations(
            UUID courseId,
            List<ScheduleSessionAllocation> allocations,
            User user
    ) {
        Result<Schedule> maybeSchedule = getScheduleIfOwnedByCr(courseId, user);
        if (!maybeSchedule.isSuccess()) {
            return TransactionalResult.rollbackFor(maybeSchedule.getError());
        }

        Schedule schedule = maybeSchedule.getData();
        boolean mismatchedSchedule = allocations.stream()
                .anyMatch(allocation -> !allocation.getSchedule().getScheduleId().equals(schedule.getScheduleId()));
        if (mismatchedSchedule) {
            return TransactionalResult.rollbackFor(ErrorCode.BAD_REQUEST.toError(
                    "All allocations must belong to the requested schedule"));
        }

        scheduleSessionAllocationRepository.deleteBySchedule(schedule);
        return TransactionalResult.ok(scheduleSessionAllocationRepository.saveAll(allocations));
    }

    private Result<Schedule> getScheduleIfOwnedByCr(UUID courseId, User user) {
        Optional<Schedule> maybeSchedule = scheduleRepository.findFirstByCourseCourseId(courseId);
        if (maybeSchedule.isEmpty()) {
            return Result.error(ErrorCode.NOT_FOUND.toError("Schedule not found"));
        }

        Schedule schedule = maybeSchedule.get();
        if (!schedule.getCourse().getOwner().getUserId().equals(user.getUserId())) {
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
        }

        return Result.ok(schedule);
    }

    private Result<Schedule> getScheduleIfUserCanViewSchedule(UUID courseId, User user) {
        Result<Course> maybeCourse;
        if (user.getUserType().equals(User.UserType.CR)) {
            maybeCourse = courseAuthorizationService.assertUserIsCrOfCourse(courseId, user);
        } else if (user.getUserType().equals(User.UserType.TA)) {
            maybeCourse = courseAuthorizationService.assertUserIsTaOfCourse(courseId, user);
        } else {
            return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
        }

        if (!maybeCourse.isSuccess()) {
            return Result.error(maybeCourse.getError());
        }

        return Result.ofOptional(
                scheduleRepository.findFirstByCourse(maybeCourse.getData()),
                ErrorCode.NOT_FOUND.toError("Schedule not found")
        );
    }
}
