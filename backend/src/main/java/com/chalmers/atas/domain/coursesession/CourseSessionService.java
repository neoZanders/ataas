package com.chalmers.atas.domain.coursesession;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseSessionService {

    private final CourseSessionRepository courseSessionRepository;

    public Result<List<CourseSession>> getCourseSessions(UUID courseId) {
        return Result.ok(courseSessionRepository.findAllByCourseCourseId(courseId));
    }

    public TransactionalResult<CourseSession> createCourseSession(
            Course course,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            CourseSession.CourseSessionType courseSessionType,
            int minTAs,
            int maxTAs,
            boolean isWeeklyRecurring) {
        return TransactionalResult.ok(courseSessionRepository.save(CourseSession.of(
                course,
                startDateTime,
                endDateTime,
                courseSessionType,
                minTAs,
                maxTAs,
                isWeeklyRecurring
        )));
    }

    public TransactionalResult<Void> deleteCourseSession(UUID courseSessionId) {
        return TransactionalResult.ofOptional(
                courseSessionRepository.findById(courseSessionId), ErrorCode.COURSE_SESSION_NOT_FOUND.toError()
        ).then(courseSessionRepository::delete);
    }
}
