package com.chalmers.atas.domain.schedulesessionallocation;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.schedule.ScheduleRepository;
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
public class ScheduleSessionAllocationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleSessionAllocationRepository scheduleSessionAllocationRepository;

    public Result<List<ScheduleSessionAllocation>> getAllocations(
            Schedule schedule,
            User user,
            boolean canTAsSeeAllSchedules) {
        if (user.getUserType().equals(User.UserType.CR) || canTAsSeeAllSchedules) {
            return Result.ok(scheduleSessionAllocationRepository.findBySchedule(schedule));
        } else {
            return Result.ok(scheduleSessionAllocationRepository.findByScheduleAndTaCourseAssignmentTa(schedule, user));
        }
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
            return TransactionalResult.rollbackFor(BAD_REQUEST,
                    "All allocations must belong to the requested schedule"
            );
        }

        scheduleSessionAllocationRepository.deleteBySchedule(schedule);

        return TransactionalResult.ok(
                scheduleSessionAllocationRepository.saveAll(allocations)
        );
    }

    private Result<Schedule> getScheduleIfOwnedByCr(UUID courseId, User user) {
        Optional<Schedule> maybeSchedule = scheduleRepository.findFirstByCourseCourseId(courseId);
        if (maybeSchedule.isEmpty()) {
            return Result.errorFromCode(NOT_FOUND, "Schedule not found");
        }

        Schedule schedule = maybeSchedule.get();

        if (!schedule.getCourse().getOwner().getUserId().equals(user.getUserId())) {
            return Result.errorFromCode(USER_NOT_COURSE_RESPONSIBLE);
        }

        return Result.ok(schedule);
    }
}