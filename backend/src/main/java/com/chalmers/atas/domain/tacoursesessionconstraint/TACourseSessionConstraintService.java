package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TACourseSessionConstraintService {

    private final TACourseSessionConstraintRepository taCourseSessionConstraintRepository;

    public Result<List<TACourseSessionConstraint>> getCourseConstraints(Course course, Optional<String> maybeUsername) {
        return Result.ok(maybeUsername.map(username ->
                taCourseSessionConstraintRepository.findAllByTaCourseAssignmentCourseAndTaCourseAssignmentTaName(course, username)
        ).orElse(taCourseSessionConstraintRepository.findAllByTaCourseAssignment_Course(course)));
    }

    public Result<List<TACourseSessionConstraint>> getTAConstraints(Course course, UUID taId) {
        return Result.ok(
                taCourseSessionConstraintRepository
                        .findAllByTaCourseAssignment_Ta_UserIdAndTaCourseAssignment_Course(taId, course)
        );
    }

    public Result<TACourseSessionConstraint> getConstraint(UUID taCourseSessionConstraintId) {
        return Result.ofOptional(
                taCourseSessionConstraintRepository.findById(taCourseSessionConstraintId),
                ErrorCode.TA_CONSTRAINT_NOT_FOUND.toError()
        );
    }

    @Transactional
    public TransactionalResult<TACourseSessionConstraint> createConstraint(
            TACourseAssignment taCourseAssignment,
            ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            boolean isWeeklyRecurring
    ) {
        if (startDateTime.isAfter(endDateTime)) {
            return TransactionalResult.rollbackFor(ErrorCode.START_AFTER_END.toError());
        }

        return TransactionalResult.ok(taCourseSessionConstraintRepository.save(
                TACourseSessionConstraint.of(
                        taCourseAssignment,
                        constraintType,
                        startDateTime,
                        endDateTime,
                        isWeeklyRecurring
                )));
    }

    @Transactional
    public TransactionalResult<TACourseSessionConstraint> updateConstraint(
            TACourseSessionConstraint constraint,
            ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Boolean isWeeklyRecurring
    ) {
        if (constraintType != null) {
            constraint.setConstraintType(constraintType);
        }
        if (startDateTime != null) {
            constraint.setStartDateTime(startDateTime);
        }
        if (endDateTime != null) {
            constraint.setEndDateTime(endDateTime);
        }
        if (isWeeklyRecurring != null) {
            constraint.setWeeklyRecurring(isWeeklyRecurring);
        }

        if (constraint.getStartDateTime().isAfter(constraint.getEndDateTime())) {
            return TransactionalResult.rollbackFor(ErrorCode.START_AFTER_END.toError());
        }

        return TransactionalResult.ok(taCourseSessionConstraintRepository.save(constraint));
    }

    @Transactional
    public TransactionalResult<Void> deleteConstraint(TACourseSessionConstraint constraint) {
        taCourseSessionConstraintRepository.delete(constraint);
        return TransactionalResult.ok();
    }
}
