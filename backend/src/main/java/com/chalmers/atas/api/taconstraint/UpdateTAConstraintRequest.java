package com.chalmers.atas.api.taconstraint;

import java.time.LocalDateTime;

import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTAConstraintRequest {

    private ConstraintType constraintType;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private Boolean isWeeklyRecurring;
}
