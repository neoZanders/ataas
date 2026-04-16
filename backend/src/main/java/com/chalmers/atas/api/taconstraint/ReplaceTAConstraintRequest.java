package com.chalmers.atas.api.taconstraint;

import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReplaceTAConstraintRequest {

    private UUID taCourseConstraintId;

    @NotNull
    private TACourseSessionConstraint.ConstraintType constraintType;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    private Boolean isWeeklyRecurring;
}
