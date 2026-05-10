package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

abstract class CourseIntegrationTestBase extends UserIntegrationTestBase {

    @Autowired
    protected CourseRepository courseRepository;

    protected CourseBuilder createCourse() {
        return new CourseBuilder();
    }

    protected class CourseBuilder {
        private String courseCode = unique("DAT");
        private User owner;
        private String description = "Test course description";
        private boolean canTASeeAllSchedules = false;
        private boolean canTACreateAnnouncements = false;
        private LocalDate startDate = LocalDate.of(2026, 1, 19);
        private LocalDate endDate = LocalDate.of(2026, 3, 22);

        public CourseBuilder withCourseCode(String courseCode) {
            this.courseCode = courseCode;
            return this;
        }

        public CourseBuilder withOwner(User owner) {
            this.owner = owner;
            return this;
        }

        public CourseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CourseBuilder withCanTASeeAllSchedules(boolean canTASeeAllSchedules) {
            this.canTASeeAllSchedules = canTASeeAllSchedules;
            return this;
        }

        public CourseBuilder withCanTACreateAnnouncements(boolean canTACreateAnnouncements) {
            this.canTACreateAnnouncements = canTACreateAnnouncements;
            return this;
        }

        public CourseBuilder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public CourseBuilder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        protected Course build() {
            if (owner == null) {
                owner = createUser()
                        .asCR()
                        .persist();
            }

            return Course.of(
                    courseCode,
                    owner,
                    description,
                    canTASeeAllSchedules,
                    canTACreateAnnouncements,
                    startDate,
                    endDate
            );
        }

        public Course persist() {
            return courseRepository.save(build());
        }
    }
}