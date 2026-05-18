package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chalmers.atas.common.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Transactional
    public TransactionalResult<Schedule> createSchedule(Course course) {
        Schedule schedule = scheduleRepository.findByCourse(course)
                .orElseGet(() -> scheduleRepository.save(Schedule.of(course)));
        return TransactionalResult.ok(schedule);
    }

    public Result<Schedule> getSchedule(Course course) {
        return Result.ofOptional(scheduleRepository.findByCourse(course), SCHEDULE_NOT_FOUND);
    }
}
