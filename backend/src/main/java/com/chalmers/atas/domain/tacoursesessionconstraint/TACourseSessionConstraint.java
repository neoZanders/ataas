package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ta_course_session_constraints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TACourseSessionConstraint implements Serializable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID taCourseSessionConstraintId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ta_course_assignment_id", nullable = false)
    private TACourseAssignment taCourseAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConstraintType constraintType;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private boolean isWeeklyRecurring;

    public enum ConstraintType {
        SOFT,
        HARD
    }

    public static TACourseSessionConstraint of(
            TACourseAssignment taCourseAssignment,
            ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            boolean isWeeklyRecurring
    ) {
        TACourseSessionConstraint constraint = new TACourseSessionConstraint();
        constraint.taCourseAssignment = taCourseAssignment;
        constraint.constraintType = constraintType;
        constraint.startDateTime = startDateTime;
        constraint.endDateTime = endDateTime;
        constraint.isWeeklyRecurring = isWeeklyRecurring;
        return constraint;
    }
}
