package com.chalmers.atas.api.taconstraint;

import java.time.LocalDateTime;

import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTAConstraintRequest {

    @NotNull
    private ConstraintType constraintType;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    private Boolean isWeeklyRecurring;
}
