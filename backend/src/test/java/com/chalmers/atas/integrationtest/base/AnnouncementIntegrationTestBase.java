package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.announcement.Announcement;
import com.chalmers.atas.domain.announcement.AnnouncementRepository;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;

abstract class AnnouncementIntegrationTestBase extends CourseIntegrationTestBase {

    @Autowired
    protected AnnouncementRepository announcementRepository;

    protected AnnouncementBuilder createAnnouncement() {
        return new AnnouncementBuilder();
    }

    protected class AnnouncementBuilder {
        private Course course;
        private User owner;
        private String title = "Test announcement";
        private String body = "This is a test announcement.";
        private boolean sendByEmail = false;

        public AnnouncementBuilder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public AnnouncementBuilder withOwner(User owner) {
            this.owner = owner;
            return this;
        }

        public AnnouncementBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public AnnouncementBuilder withBody(String body) {
            this.body = body;
            return this;
        }

        public AnnouncementBuilder withSendByEmail(boolean sendByEmail) {
            this.sendByEmail = sendByEmail;
            return this;
        }

        protected Announcement build() {
            if (course == null) {
                course = createCourse().persist();
            }

            if (owner == null) {
                owner = course.getOwner();
            }

            return Announcement.of(
                    course,
                    owner,
                    title,
                    body,
                    sendByEmail
            );
        }

        public Announcement persist() {
            return announcementRepository.save(build());
        }
    }
}