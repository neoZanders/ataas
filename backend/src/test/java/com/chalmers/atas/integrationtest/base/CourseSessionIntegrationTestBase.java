package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.coursesession.CourseSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

abstract class CourseSessionIntegrationTestBase extends AnnouncementIntegrationTestBase {

    @Autowired
    protected CourseSessionRepository courseSessionRepository;

    protected CourseSessionBuilder createCourseSession() {
        return new CourseSessionBuilder();
    }

    protected class CourseSessionBuilder {
        private Course course;
        private LocalDateTime startDateTime = LocalDateTime.of(2026, 1, 19, 10, 0);
        private LocalDateTime endDateTime = LocalDateTime.of(2026, 1, 19, 12, 0);
        private CourseSession.CourseSessionType sessionType = CourseSession.CourseSessionType.LABORATION;
        private int minTAs = 1;
        private int maxTAs = 2;
        private boolean isWeeklyRecurring = false;

        public CourseSessionBuilder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public CourseSessionBuilder withStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public CourseSessionBuilder withEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public CourseSessionBuilder withSessionType(CourseSession.CourseSessionType sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        public CourseSessionBuilder withMinTAs(int minTAs) {
            this.minTAs = minTAs;
            return this;
        }

        public CourseSessionBuilder withMaxTAs(int maxTAs) {
            this.maxTAs = maxTAs;
            return this;
        }

        public CourseSessionBuilder withIsWeeklyRecurring(boolean isWeeklyRecurring) {
            this.isWeeklyRecurring = isWeeklyRecurring;
            return this;
        }

        protected CourseSession build() {
            if (course == null) {
                course = createCourse().persist();
            }

            return CourseSession.of(
                    course,
                    startDateTime,
                    endDateTime,
                    sessionType,
                    minTAs,
                    maxTAs,
                    isWeeklyRecurring
            );
        }

        public CourseSession persist() {
            return courseSessionRepository.save(build());
        }
    }
}
