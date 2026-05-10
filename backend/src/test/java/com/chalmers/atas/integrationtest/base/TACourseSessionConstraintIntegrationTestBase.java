package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraintRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

abstract class TACourseSessionConstraintIntegrationTestBase extends TACourseAssignmentIntegrationTestBase {

    @Autowired
    protected TACourseSessionConstraintRepository taCourseSessionConstraintRepository;

    protected TACourseSessionConstraintBuilder createTACourseSessionConstraint() {
        return new TACourseSessionConstraintBuilder();
    }

    protected class TACourseSessionConstraintBuilder {
        private TACourseAssignment taCourseAssignment;
        private TACourseSessionConstraint.ConstraintType constraintType = TACourseSessionConstraint.ConstraintType.HARD;
        private LocalDateTime startDateTime = LocalDateTime.of(2026, 1, 20, 8, 0);
        private LocalDateTime endDateTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        private boolean isWeeklyRecurring = false;

        public TACourseSessionConstraintBuilder withTACourseAssignment(TACourseAssignment taCourseAssignment) {
            this.taCourseAssignment = taCourseAssignment;
            return this;
        }

        public TACourseSessionConstraintBuilder withConstraintType(TACourseSessionConstraint.ConstraintType constraintType) {
            this.constraintType = constraintType;
            return this;
        }

        public TACourseSessionConstraintBuilder asHard() {
            this.constraintType = TACourseSessionConstraint.ConstraintType.HARD;
            return this;
        }

        public TACourseSessionConstraintBuilder asSoft() {
            this.constraintType = TACourseSessionConstraint.ConstraintType.SOFT;
            return this;
        }

        public TACourseSessionConstraintBuilder withStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public TACourseSessionConstraintBuilder withEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public TACourseSessionConstraintBuilder withIsWeeklyRecurring(boolean isWeeklyRecurring) {
            this.isWeeklyRecurring = isWeeklyRecurring;
            return this;
        }

        protected TACourseSessionConstraint build() {
            if (taCourseAssignment == null) {
                taCourseAssignment = createTACourseAssignment().persist();
            }

            return TACourseSessionConstraint.of(
                    taCourseAssignment,
                    constraintType,
                    startDateTime,
                    endDateTime,
                    isWeeklyRecurring
            );
        }

        public TACourseSessionConstraint persist() {
            return taCourseSessionConstraintRepository.save(build());
        }
    }
}
