package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignment;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentRepository;
import com.chalmers.atas.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;

abstract class CRCourseAssignmentIntegrationTestBase extends CourseSessionIntegrationTestBase {

    @Autowired
    protected CRCourseAssignmentRepository crCourseAssignmentRepository;

    protected CRCourseAssignmentBuilder createCRCourseAssignment() {
        return new CRCourseAssignmentBuilder();
    }

    protected class CRCourseAssignmentBuilder {
        private Course course;
        private User cr;
        private CourseAssignmentStatus status = CourseAssignmentStatus.JOINED;

        public CRCourseAssignmentBuilder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public CRCourseAssignmentBuilder withCR(User cr) {
            this.cr = cr;
            return this;
        }

        public CRCourseAssignmentBuilder withStatus(CourseAssignmentStatus status) {
            this.status = status;
            return this;
        }

        public CRCourseAssignmentBuilder asOwner() {
            this.status = CourseAssignmentStatus.OWNER;
            return this;
        }

        public CRCourseAssignmentBuilder asJoined() {
            this.status = CourseAssignmentStatus.JOINED;
            return this;
        }

        public CRCourseAssignmentBuilder asInvited() {
            this.status = CourseAssignmentStatus.INVITED;
            return this;
        }

        protected CRCourseAssignment build() {
            if (course == null) {
                course = createCourse().persist();
            }

            if (cr == null) {
                cr = createUser()
                        .asCR()
                        .persist();
            }

            return CRCourseAssignment.of(
                    cr,
                    course,
                    status
            );
        }

        public CRCourseAssignment persist() {
            return crCourseAssignmentRepository.save(build());
        }
    }
}
