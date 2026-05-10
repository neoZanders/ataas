package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentRepository;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

abstract class TACourseAssignmentIntegrationTestBase extends CRCourseAssignmentIntegrationTestBase {

    @Autowired
    protected TACourseAssignmentRepository taCourseAssignmentRepository;

    protected TACourseAssignmentBuilder createTACourseAssignment() {
        return new TACourseAssignmentBuilder();
    }

    protected class TACourseAssignmentBuilder {
        private Course course;
        private User ta;
        private CourseAssignmentStatus status = CourseAssignmentStatus.JOINED;
        private int minHours = 10;
        private int maxHours = 20;
        private List<CourseSession.CourseSessionType> sessionTypePreferences = List.of(
                CourseSession.CourseSessionType.LABORATION,
                CourseSession.CourseSessionType.GRADING,
                CourseSession.CourseSessionType.HELP,
                CourseSession.CourseSessionType.EXERCISE
        );
        private boolean preferCompactSchedule = false;

        public TACourseAssignmentBuilder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public TACourseAssignmentBuilder withTA(User ta) {
            this.ta = ta;
            return this;
        }

        public TACourseAssignmentBuilder asJoined() {
            this.status = CourseAssignmentStatus.JOINED;
            return this;
        }

        public TACourseAssignmentBuilder asInvited() {
            this.status = CourseAssignmentStatus.INVITED;
            return this;
        }

        public TACourseAssignmentBuilder withMinHours(int minHours) {
            this.minHours = minHours;
            return this;
        }

        public TACourseAssignmentBuilder withMaxHours(int maxHours) {
            this.maxHours = maxHours;
            return this;
        }

        public TACourseAssignmentBuilder withSessionTypePreferences(List<CourseSession.CourseSessionType> sessionTypePreferences) {
            this.sessionTypePreferences = sessionTypePreferences;
            return this;
        }

        public TACourseAssignmentBuilder withPreferCompactSchedule(boolean preferCompactSchedule) {
            this.preferCompactSchedule = preferCompactSchedule;
            return this;
        }

        protected TACourseAssignment build() {
            if (course == null) {
                course = createCourse().persist();
            }

            if (ta == null) {
                ta = createUser()
                        .asTA()
                        .persist();
            }

            while (sessionTypePreferences.size() < 4) {
                sessionTypePreferences.add(CourseSession.CourseSessionType.LABORATION);
            }

            return TACourseAssignment.of(
                    ta,
                    course,
                    status,
                    minHours,
                    maxHours,
                    sessionTypePreferences.get(0),
                    sessionTypePreferences.get(1),
                    sessionTypePreferences.get(2),
                    sessionTypePreferences.get(3),
                    preferCompactSchedule
            );
        }

        public TACourseAssignment persist() {
            return taCourseAssignmentRepository.save(build());
        }
    }
}
