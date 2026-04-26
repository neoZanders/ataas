package com.chalmers.atas.api.taconstraint;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TAConstraintResponse {

    private UUID taCourseSessionConstraintId;

    private ConstraintType constraintType;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private boolean isWeeklyRecurring;

    public static TAConstraintResponse of(TACourseSessionConstraint constraint) {
        return new TAConstraintResponse(
                constraint.getTaCourseSessionConstraintId(),
                constraint.getConstraintType(),
                constraint.getStartDateTime(),
                constraint.getEndDateTime(),
                constraint.isWeeklyRecurring()
        );
    }
}
